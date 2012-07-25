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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMetaModel implements Serializable {

  /** . */
  HashMap<String, String> targets;

  HashMap<String, RouteMetaModel> children;

  void setTarget(String key, String target) {
    if (target == null) {
      if (targets != null) {
        targets.remove(key);
      }
    } else {
      if (targets == null) {
        targets = new HashMap<String, String>();
      }
      targets.put(key, target);
    }
  }

  RouteMetaModel addChild(String path) {
    if (children == null) {
      children = new HashMap<String, RouteMetaModel>();
    }
    RouteMetaModel child = children.get(path);
    if (child == null) {
      children.put(path, child = new RouteMetaModel());
    }
    return child;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("targets", targets != null ? targets : Collections.<String, String>emptyMap());
    json.set("children", children != null ? children : Collections.<String, RouteMetaModel>emptyMap());
    return json;
  }
}
