/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.bridge.spi.portlet;

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
  private final ResourceResolver resolver;

  public PortletModuleContext(PortletContext portletContext, ClassLoader classLoader) {
    this.portletContext = portletContext;
    this.classLoader = classLoader;
    this.resolver = new ResourceResolver() {
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

  public ResourceResolver getServerResolver() {
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
