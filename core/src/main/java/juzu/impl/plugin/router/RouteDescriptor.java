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
import juzu.impl.metadata.Descriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor extends Descriptor {

  /** . */
  private final Map<String, String> targets;

  /** . */
  private final Map<String, RouteDescriptor> children;

  public RouteDescriptor(JSON json) {

    JSON targets = json.getJSON("targets");
    if (targets != null) {
      Set<String> names = targets.names();
      if (names.size() > 0) {
        this.targets = new HashMap<String, String>();
        for (String name : names) {
          String value = targets.getString(name);
          this.targets.put(name, value);
        }
      } else {
        this.targets = Collections.emptyMap();
      }
    } else {
      this.targets = Collections.emptyMap();
    }

    //
    JSON children = json.getJSON("children");
    if (children != null) {
      Set<String> names = children.names();
      if (names.size() > 0) {
        this.children = new HashMap<String, RouteDescriptor>();
        for (String name : names) {
          JSON value = children.getJSON(name);
          this.children.put(name, new RouteDescriptor(value));
        }
      } else {
        this.children = Collections.emptyMap();
      }
    } else {
      this.children = Collections.emptyMap();
    }
  }

  public Map<String, String> getTargets() {
    return targets;
  }

  public Map<String, RouteDescriptor> getChildren() {
    return children;
  }
}
