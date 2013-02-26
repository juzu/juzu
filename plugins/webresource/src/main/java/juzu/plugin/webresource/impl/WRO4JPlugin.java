/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package juzu.plugin.webresource.impl;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.StandardLocation;

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;
import juzu.plugin.webresource.Util;
import juzu.plugin.webresource.annotation.Groups;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/19/13
 */
public class WRO4JPlugin extends ModuleMetaModelPlugin {

    private static final Logger log = BaseProcessor.getLogger(WRO4JPlugin.class);

    /**
     * Mapping resource-array elements in @Group to type of associated output.
     */
    private static final Map<String, ResultType> ELEMENT_OF_GROUP_TO_RESULT_MAP = new HashMap<String, ResultType>();

    static {
        ELEMENT_OF_GROUP_TO_RESULT_MAP.put("javascripts", ResultType.JS);
        ELEMENT_OF_GROUP_TO_RESULT_MAP.put("lesses", ResultType.CSS);
        ELEMENT_OF_GROUP_TO_RESULT_MAP.put("stylesheets", ResultType.CSS);
    }

    private Map<Name, AnnotationState> annotations;


    public WRO4JPlugin() {
        super("WRO4JPlugin");
    }

    @Override
    public void init(ModuleMetaModel metaModel) {
        annotations = new HashMap<Name, AnnotationState>();
    }

    @Override
    public Set<Class<? extends Annotation>> init(ProcessingContext env) {
        return Collections.<Class<? extends Annotation>>singleton(Groups.class);
    }

    @Override
    public void processAnnotationAdded(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState added) {
        Name pkg = key.getElement().getPackage();
        log.log("Adding Groups annotation for package " + pkg);
        annotations.put(pkg, added);
    }

    @Override
    public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
        Name pkg = key.getElement().getPackage();
        log.log("Removing Groups annotation for package " + pkg);
        annotations.remove(pkg);
    }

    @Override
    public void postActivate(ModuleMetaModel metaModel) {
        annotations = new HashMap<Name, AnnotationState>();
    }

    @Override
    public void prePassivate(ModuleMetaModel metaModel) {
        Map<Name, AnnotationState> clone = annotations;
        annotations = null;
        ProcessingContext ctx = metaModel.processingContext;

        for (Map.Entry<Name, AnnotationState> entry : clone.entrySet()) {
            AnnotationState annotation = entry.getValue();
            List<ElementHandle.Class> preProcsMetadata = (List<ElementHandle.Class>) annotation.get("preProcessors");
            List<ElementHandle.Class> postProcsMetadata = (List<ElementHandle.Class>) annotation.get("postProcessors");
            final List<ResourcePreProcessor> preProcs = Util.buildInstances(preProcsMetadata, ResourcePreProcessor.class);
            final List<ResourcePostProcessor> postProcs = Util.buildInstances(postProcsMetadata, ResourcePostProcessor.class);

            Name pkg = entry.getKey();
            Name wro4jPkg = pkg.append("wro4j");

            ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
            //DEFAULT VALUE IS NOT RETRIEVABLE WITH AnnotationState!!!
            String targetDir = (annotation.get("targetDir") != null) ? (String) annotation.get("targetDir") : "wro4j";
            List<AnnotationState> groups = (List<AnnotationState>) annotation.get("groups");
            for (AnnotationState group : groups) {
                Collection<ResultBuilder> builders = createResultBuildersForGroup(group, preProcs, postProcs, wro4jPkg, pkgHandle, ctx);
                List<Result> optimizedOutput = getOptimizedOutput(builders);
                writeOptimizedOutput(optimizedOutput, targetDir, ctx);
            }
        }
    }

    /**
     * Create a collection of builders for resources declared as elements of @Group
     *
     * @param group
     * @param preProcessors
     * @param postProcessors
     * @param wro4jPkg
     * @return
     * @throws Exception
     */
    private Collection<ResultBuilder> createResultBuildersForGroup(AnnotationState group, List<ResourcePreProcessor> preProcessors, List<ResourcePostProcessor> postProcessors, Name wro4jPkg, ElementHandle.Package pkgHandle, ProcessingContext processingContext) {
        Map<ResultType, ResultBuilder> builders = new HashMap<ResultType, ResultBuilder>();

        for (Map.Entry<String, ResultType> entry : ELEMENT_OF_GROUP_TO_RESULT_MAP.entrySet()) {
            String elementName = entry.getKey();
            List<AnnotationState> annons = (List<AnnotationState>) group.get(elementName);
            if (annons != null && annons.size() > 0) {
                ResultType resultType = entry.getValue();
                /**
                 * Keep in mind that lesses/stylesheets resources belonging to a group
                 * are processed and merged into a single file. That explains why we
                 * look at the map first
                 */
                ResultBuilder builder = builders.get(resultType);
                if (builder == null) {
                    String name = (String) group.get("name");
                    log.log("Create ResultBuilder with type " + resultType.name() + " for group named " + name);
                    builder = new ResultBuilder(name, resultType);
                    builder.withPreProcessors(preProcessors);
                    builder.withPostProcessors(postProcessors);
                    builders.put(resultType, builder);
                }

                for (AnnotationState annon : annons) {
                    Path path = Path.parse((String) annon.get("path"));
                    Path.Absolute absolute = wro4jPkg.resolve(path);
                    FileObject f = processingContext.resolveResource(pkgHandle, absolute);
                    Resource r = Resource.create(f.toUri().toString());
                    builder.withResource(r);
                }
            }
        }

        return builders.values();
    }

    private List<Result> getOptimizedOutput(Collection<ResultBuilder> builders) {
        List<Result> re = new LinkedList<Result>();

        for (ResultBuilder builder : builders) {
            log.log("Get optimized output for " + builder.type.name() + " of group " + builder.name);
            Result result = builder.build();
            if (result instanceof Result.ErrorResult) {
                log.log("Error while optimize output for " + builder.type.name() + " of group " + builder.name + "\n Cause: " + result.getContent());
            } else if (result instanceof Result.MergedResult) {
                re.add(result);
            }
        }

        return re;
    }

    private void writeOptimizedOutput(List<Result> optimizedOutput, String targetDir, ProcessingContext processingEnv) {
        for (Result r : optimizedOutput) {
            Writer writer = null;
            try {
                FileObject f = processingEnv.createResource(StandardLocation.CLASS_OUTPUT, targetDir, r.name + r.type.getExtension());
                writer = f.openWriter();
                writer.append(r.getContent());
            } catch (IOException ioEx) {
                log.log("IO exception while write optimized output for " + r.type.name() + " of group " + r.name);
            } finally {
                Tools.safeClose(writer);
            }
        }
    }
}
