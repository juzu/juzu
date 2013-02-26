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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import juzu.impl.common.Tools;
import juzu.plugin.webresource.Util;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/25/13
 */
public class ResultBuilder {

    /* package */ final String name;

    /* package */ final ResultType type;

    private final Map<ResourcePreProcessor, List<ResourceType>> preProcessors;

    private final Map<ResourcePostProcessor, List<ResourceType>> postProcessors;

    private final List<Resource> resources;

    public ResultBuilder(String _name, ResultType _type) {
        name = _name;
        type = _type;
        resources = new LinkedList<Resource>();
        preProcessors = new LinkedHashMap<ResourcePreProcessor, List<ResourceType>>();
        postProcessors = new LinkedHashMap<ResourcePostProcessor, List<ResourceType>>();

    }

    public ResultBuilder withResources(List<Resource> rs) {
        resources.addAll(rs);
        return this;
    }

    public ResultBuilder withResource(Resource r) {
        resources.add(r);
        return this;
    }

    public ResultBuilder withPreProcessors(List<ResourcePreProcessor> preProcs) {
        for (ResourcePreProcessor preProc : preProcs) {
            withPreProcessor(preProc);
        }
        return this;
    }

    public ResultBuilder withPreProcessor(ResourcePreProcessor preProc) {
        preProcessors.put(preProc, Util.getSupportedResourceTypes(preProc));
        return this;
    }

    public ResultBuilder withPostProcessors(List<ResourcePostProcessor> postProcs) {
        for (ResourcePostProcessor postProc : postProcs) {
            withPostProcessor(postProc);
        }
        return this;
    }

    public ResultBuilder withPostProcessor(ResourcePostProcessor postProc) {
        postProcessors.put(postProc, Util.getSupportedResourceTypes(postProc));
        return this;
    }

    public final Result build() {
        StringBuilder merged = new StringBuilder();

        /**
         * Pre-process each individual Resource and merge pre-processed content into final output
         */
        for (Resource resource : resources) {
            String tobePreProcessed;
            try {
                tobePreProcessed = Util.getContent(resource, "UTF-8");
            } catch (Exception ex) {
                return new Result.ErrorResult(name, "Exception while load content of " + resource.getUri(), ex);
            }

            for (Map.Entry<ResourcePreProcessor, List<ResourceType>> entry : preProcessors.entrySet()) {
                if (entry.getValue().contains(resource.getType())) {
                    ResourcePreProcessor preProc = entry.getKey();
                    StringReader tmpReader = new StringReader(tobePreProcessed);
                    StringWriter tmpWriter = new StringWriter();
                    try {
                        preProc.process(resource, tmpReader, tmpWriter);
                        tobePreProcessed = tmpWriter.toString();
                    } catch (IOException ioEx) {
                        return new Result.ErrorResult(name, "Exception while pre-process resource " + resource.getUri() + " with preProcessor " + preProc.getClass().getCanonicalName(), ioEx);
                    } finally {
                        Tools.safeClose(tmpReader);
                        Tools.safeClose(tmpWriter);
                    }
                }
            }

            merged.append(tobePreProcessed);
        }

        /**
         * Apply post-processors to merged content
         */
        if (postProcessors.size() == 0) {
            return new Result.MergedResult(name, type, merged);
        } else {
            String tobePostProcessed = merged.toString();

            for (Map.Entry<ResourcePostProcessor, List<ResourceType>> entry : postProcessors.entrySet()) {
                if (entry.getValue().contains(ResourceType.get(type.name()))) {
                    StringReader tmpReader = new StringReader(tobePostProcessed);
                    StringWriter tmpWriter = new StringWriter();
                    ResourcePostProcessor postProc = entry.getKey();
                    try {
                        postProc.process(tmpReader, tmpWriter);
                        tobePostProcessed = tmpWriter.toString();
                    } catch (IOException ioEx) {
                        return new Result.ErrorResult(name, "Exception while post-process merged content with postProcessor " + postProc.getClass().getCanonicalName(), ioEx);
                    } finally {
                        Tools.safeClose(tmpReader);
                        Tools.safeClose(tmpWriter);
                    }
                }
            }

            return new Result.MergedResult(name, type, new StringBuilder(tobePostProcessed));
        }
    }
}
