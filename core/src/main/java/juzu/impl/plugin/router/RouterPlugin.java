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

package juzu.impl.plugin.router;

import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterPlugin extends ApplicationPlugin {

  /** . */
  private RouteDescriptor descriptor;

  public RouterPlugin() {
    super("router");
  }

  @Override
  public Descriptor init(PluginContext context) throws Exception {
    if (context.getConfig() != null) {
      descriptor = new RouteDescriptor(context.getConfig());
    }
    return descriptor;
  }

  public RouteDescriptor getDescriptor() {
    return descriptor;
  }
}
