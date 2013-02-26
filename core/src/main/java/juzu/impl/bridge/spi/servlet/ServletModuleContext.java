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

package juzu.impl.bridge.spi.servlet;

import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.plugin.module.AbstractWarModuleContext;
import juzu.impl.resource.ResourceResolver;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletModuleContext extends AbstractWarModuleContext {

  /** . */
  final ServletContext servletContext;

  /** . */
  final ResourceResolver resolver;

  public ServletModuleContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    this.resolver = new ResourceResolver() {
      public URL resolve(String uri) {
        try {
          return ServletModuleContext.this.servletContext.getResource(uri);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    };
  }

  public ClassLoader getClassLoader() {
    return servletContext.getClassLoader();
  }

  public ResourceResolver getServerResolver() {
    return resolver;
  }

  @Override
  protected WarFileSystem createFileSystem(String mountPoint) {
    return WarFileSystem.create(servletContext, mountPoint);
  }

  @Override
  protected String getInitParameter(String parameterName) {
    return servletContext.getInitParameter(parameterName);
  }
}
