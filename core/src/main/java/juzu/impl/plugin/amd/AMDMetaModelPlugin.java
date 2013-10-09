/*
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
package juzu.impl.plugin.amd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.StandardLocation;

import juzu.asset.AssetLocation;
import juzu.impl.common.JSON;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.plugin.amd.Defines;
import juzu.plugin.amd.Requires;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AMDMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private HashMap<ElementHandle.Package, AnnotationState> defines =
    new HashMap<ElementHandle.Package, AnnotationState>();

  /** . */
  private HashMap<ElementHandle.Package, AnnotationState> requires =
    new HashMap<ElementHandle.Package, AnnotationState>();

  public AMDMetaModelPlugin() {
    super("amd");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.set(Defines.class, Requires.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(Name.create(Defines.class))) {
      defines.put(metaModel.getHandle(), added);
    } else if (key.getType().equals(Name.create(Requires.class))) {
      requires.put(metaModel.getHandle(), added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(Name.create(Defines.class))) {
      defines.remove(metaModel.getHandle());
    } else if (key.getType().equals(Name.create(Requires.class))) {
      requires.remove(metaModel.getHandle());
    }
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    AnnotationState defineState = defines.get(metaModel.getHandle());
    process(defineState, metaModel);
    AnnotationState requireState = requires.get(metaModel.getHandle());
    process(requireState, metaModel);
  }

  private void process(AnnotationState annotation, ApplicationMetaModel metaModel) {
    if (annotation != null) {
      String location = (String)annotation.get("location");
      boolean classpath = location == null || AssetLocation.APPLICATION.equals(AssetLocation.safeValueOf(location));
      List<AnnotationState> modules = (List<AnnotationState>)annotation.get("value");
      ProcessingContext context = metaModel.getProcessingContext();
      if (modules != null) {
        for (AnnotationState module : modules) {
          location = (String)module.get("location");
          if ((location == null && classpath) || AssetLocation.APPLICATION.equals(AssetLocation.safeValueOf(location))) {
            String value = (String)module.get("path");
            Path path = Path.parse(value);
            if (path.isRelative()) {
              context.log("Found classpath asset to copy " + value);
              Name qn = metaModel.getHandle().getPackageName().append("assets");
              Path.Absolute absolute = qn.resolve(path);
              FileObject src = context.resolveResourceFromSourcePath(metaModel.getHandle(), absolute);
              if (src != null) {
                URI srcURI = src.toUri();
                context.log("Found asset " + absolute + " on source path " + srcURI);
                InputStream in = null;
                OutputStream out = null;
                try {
                  FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, absolute);
                  if (dst == null || dst.getLastModified() < src.getLastModified()) {
                    in = src.openInputStream();
                    dst =
                      context.createResource(StandardLocation.CLASS_OUTPUT, absolute,
                        context.get(metaModel.getHandle()));
                    context.log("Copying asset from source path " + srcURI + " to class output " + dst.toUri());
                    out = dst.openOutputStream();
                    Tools.copy(in, out);
                  } else {
                    context.log("Found up to date related asset in class output for " + srcURI);
                  }
                } catch (IOException e) {
                  context.log("Could not copy asset " + path + " ", e);
                } finally {
                  Tools.safeClose(in);
                  Tools.safeClose(out);
                }
              } else {
                context.log("Could not find asset " + absolute + " on source path");
              }
            }
          }
        }
      }
    }
  }

  private List<JSON> build(List<Map<String, Object>> scripts) {
    List<JSON> foo = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      foo = new ArrayList<JSON>(scripts.size());
      for (Map<String, Object> script : scripts) {
        JSON bar = new JSON();
        for (Map.Entry<String, Object> entry : script.entrySet()) {
          bar.set(entry.getKey(), entry.getValue());
        }
        foo.add(bar);
      }
    }
    return foo;
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    AnnotationState definesState = defines.get(application.getHandle());
    AnnotationState requiresState = requires.get(application.getHandle());
    JSON config = null;
    if (definesState != null) {
      config = new JSON();
      JSON definesJSON = new JSON();
      List<Map<String, Object>> defines = (List<Map<String, Object>>)definesState.get("value");
      definesJSON.set("value", build(defines));
      config.set("defines", definesJSON);
    }
    if (requiresState != null) {
      if (config == null) {
        config = new JSON();
      }
      JSON requiresJSON = new JSON();
      List<Map<String, Object>> requires = (List<Map<String, Object>>)requiresState.get("value");
      requiresJSON.set("value", build(requires));
      requiresJSON.set("location", requiresState.get("location"));
      config.set("requires", requiresJSON);
    }

    if (config != null) {
      config.set("package", "assets");
    }

    return config;
  }
}
