/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.spi.portlet;

import juzu.impl.common.Logger;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.plugin.module.AbstractWarModuleContext;
import juzu.impl.resource.ResourceResolver;

import javax.portlet.PortletContext;
import java.net.MalformedURLException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletModuleContext extends AbstractWarModuleContext {

  /** . */
  private final PortletContext portletContext;

  /** . */
  private final ClassLoader classLoader;

  /** . */
  private final ResourceResolver<URL> resolver;

  public PortletModuleContext(PortletContext portletContext, ClassLoader classLoader, Logger log) {
    super(log);

    //
    this.portletContext = portletContext;
    this.classLoader = classLoader;
    this.resolver = new ResourceResolver<URL>() {
      public URL resolve(String uri) {
        try {
          return PortletModuleContext.this.portletContext.getResource(uri);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    };
  }

  public ResourceResolver<URL> getServerResolver() {
    return resolver;
  }

  @Override
  protected WarFileSystem createFileSystem(String mountPoint) {
    return WarFileSystem.create(portletContext, mountPoint);
  }

  @Override
  protected String getInitParameter(String parameterName) {
    return portletContext.getInitParameter(parameterName);
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }
}
