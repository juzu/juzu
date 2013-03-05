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
package juzu.plugin.webresource;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import juzu.impl.common.Name;
import juzu.impl.compiler.ElementHandle;
import org.apache.commons.io.IOUtils;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/22/13
 */
public class Util {

    public static <T> List<T> buildInstances(List<ElementHandle.Class> compileTimeModels, Class<T> runtimeType) {
        return buildInstances(compileTimeModels, runtimeType, true);
    }

    public static <T> List<T> buildInstances(List<ElementHandle.Class> compileTimeModels, Class<T> runtimeType, boolean failOnError) {
        List<T> instances = new LinkedList<T>();
        try {
            for (ElementHandle.Class compileTimeModel : compileTimeModels) {
                Name fqn = compileTimeModel.getFQN();
                Class<? extends T> clazz = Class.forName(fqn.toString()).asSubclass(runtimeType);
                instances.add(clazz.newInstance());
            }
        } catch (Exception ex) {
            if (failOnError) {
                throw new RuntimeException(ex);
            } else {
                ex.printStackTrace();
            }
        }

        return instances;
    }

    public static String getContent(Resource resource, String charsetName) throws IOException {
        return IOUtils.toString(URI.create(resource.getUri()), charsetName);
    }

    public static String getContent(Resource resource, ClassLoader customLoader, String charsetName) throws IOException {
        final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (customLoader != currentLoader) {
                Thread.currentThread().setContextClassLoader(customLoader);
            }
            return getContent(resource, charsetName);
        } finally {
            if (customLoader != currentLoader) {
                Thread.currentThread().setContextClassLoader(currentLoader);
            }
        }
    }

    public static List<ResourceType> getSupportedResourceTypes(Object processor) {
        if (processor instanceof ResourcePreProcessor || processor instanceof ResourcePostProcessor) {
            List<ResourceType> supported = new LinkedList<ResourceType>();
            SupportedResourceType annon = processor.getClass().getAnnotation(SupportedResourceType.class);
            if (annon == null) {
                supported.add(ResourceType.JS);
                supported.add(ResourceType.CSS);
            } else {
                supported.add(annon.value());
            }
            return supported;
        } else {
            throw new IllegalArgumentException("Input must be of type ResourcePreProcessor/ResourcePostProcessor");
        }
    }
}
