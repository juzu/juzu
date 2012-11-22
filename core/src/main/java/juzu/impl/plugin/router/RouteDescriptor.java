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
import juzu.impl.common.QualifiedName;
import juzu.impl.metadata.Descriptor;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor extends Descriptor {

  /** . */
  private final String path;

  /** . */
  private final Map<String, String> targets;

  /** . */
  private final List<RouteDescriptor> children;

  /** . */
  private final HashMap<String, String> parameters;

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
    List<RouteDescriptor> abc = Collections.emptyList();
    List<? extends JSON> children = json.getList("routes", JSON.class);
    if (children != null) {
      for (JSON child : children) {
        RouteDescriptor c = new RouteDescriptor(child);
        if (abc.isEmpty()) {
          abc = new LinkedList<RouteDescriptor>();
        }
        abc.add(c);
      }
    }

    //
    HashMap<String, String> parameters = null;
    JSON foo = json.getJSON("parameters");
    if (foo != null) {
      parameters = new HashMap<String, String>();
      for (String name : foo.names()) {
        String pattern = foo.getJSON(name).getString("pattern");
        parameters.put(name, pattern);
      }
    }

    //
    this.children = abc;
    this.path = json.getString("path");
    this.parameters = parameters;
  }

  public String getPath() {
    return path;
  }

  public Map<String, String> getTargets() {
    return targets;
  }

  public List<RouteDescriptor> getChildren() {
    return children;
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public Map<RouteDescriptor, Route> popupate(Route parent) {
    Map<RouteDescriptor, Route> ret = new LinkedHashMap<RouteDescriptor, Route>();
    popupate(parent, ret);
    return ret;
  }

  public void popupate(Route parent, Map<RouteDescriptor, Route> ret) {

    //
    Map<QualifiedName, PathParam.Builder> parameters;
    if (this.parameters != null && this.parameters.size() > 0) {
      parameters = new HashMap<QualifiedName, PathParam.Builder>(this.parameters.size());
      for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
        parameters.put(QualifiedName.create(parameter.getKey()), PathParam.matching(parameter.getValue()));
      }
    } else {
      parameters = Collections.emptyMap();
    }

    //
    Route route = parent.append(path, parameters);

    //
    ret.put(this, route);

    //
    for (RouteDescriptor child : children) {
      child.popupate(route, ret);
    }
  }
}
