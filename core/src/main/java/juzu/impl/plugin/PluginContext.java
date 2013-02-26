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

package juzu.impl.plugin;

import juzu.impl.common.JSON;
import juzu.impl.resource.ResourceResolver;

/**
 * The context of a plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface PluginContext {

  /**
   * Returns the plugin configuration.
   *
   * @return the plugin configuraiton
   */
  JSON getConfig();

  /**
   * Reurns the classloader.
   *
   * @return the classloader
   */
  ClassLoader getClassLoader();

  /**
   * Returns the resource resolver for server resources.
   *
   * @return the server resolver
   */
  ResourceResolver getServerResolver();

  /**
   * Returns the resource resolver for applications resources.
   *
   * @return the application resolver
   */
  ResourceResolver getApplicationResolver();

}
