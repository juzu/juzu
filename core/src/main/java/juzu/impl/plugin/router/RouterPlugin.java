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

import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterPlugin extends Plugin {

  public RouterPlugin() {
    super("router");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    List<RouteDescriptor> routes = new ArrayList<RouteDescriptor>();
    for (JSON route : config.getList("routes", JSON.class)) {
      String target = route.getString("target");
      String path = route.getString("path");
      RouteDescriptor r = new RouteDescriptor(MethodHandle.parse(target), path);
      routes.add(r);
    }
    return new RouterDescriptor(Collections.unmodifiableList(routes));
  }
}
