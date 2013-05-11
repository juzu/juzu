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

package juzu.impl.plugin.router;

import juzu.impl.common.JSON;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.Router;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor extends PluginDescriptor {

  /** . */
  private final String path;

  /** . */
  private final Map<String, String> targets;

  /** . */
  private final List<RouteDescriptor> children;

  /** . */
  private final HashMap<String, ParamDescriptor> parameters;

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
    HashMap<String, ParamDescriptor> parameters = null;
    JSON foo = json.getJSON("parameters");
    if (foo != null) {
      parameters = new HashMap<String, ParamDescriptor>();
      for (String name : foo.names()) {
        String pattern = foo.getJSON(name).getString("pattern");
        Boolean preservePath = foo.getJSON((name)).getBoolean("preserve-path");
        Boolean captureGroup = foo.getJSON((name)).getBoolean("capture-group");
        parameters.put(name, new ParamDescriptor(pattern, preservePath, captureGroup));
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

  public HashMap<String, ParamDescriptor> getParameters() {
    return parameters;
  }

  public Map<RouteDescriptor, Route> create() {
    Map<RouteDescriptor, Route> ret = new LinkedHashMap<RouteDescriptor, Route>();
    Router router = new Router();
    ret.put(this, router);
    populateChildren(router, ret);
    return ret;
  }

  public Map<RouteDescriptor, Route> popupate(Route parent) {
    Map<RouteDescriptor, Route> ret = new LinkedHashMap<RouteDescriptor, Route>();
    popupate(parent, ret);
    return ret;
  }

  public void popupate(Route parent, Map<RouteDescriptor, Route> ret) {
    Map<String, PathParam.Builder> parameters;
    if (this.parameters != null && this.parameters.size() > 0) {
      parameters = new HashMap<String, PathParam.Builder>(this.parameters.size());
      for (Map.Entry<String, ParamDescriptor> parameter : this.parameters.entrySet()) {
        ParamDescriptor paramDescriptor = parameter.getValue();
        PathParam.Builder builder = PathParam.matching(paramDescriptor.pattern);
        if (paramDescriptor.preservePath != null) {
          builder.setPreservePath(paramDescriptor.preservePath);
        }
        if (paramDescriptor.captureGroup != null) {
          builder.setCaptureGroup(paramDescriptor.captureGroup);
        }
        parameters.put(parameter.getKey(), builder);
      }
    } else {
      parameters = Collections.emptyMap();
    }
    Route route = parent.append(path, parameters);
    ret.put(this, route);

    //
    populateChildren(route, ret);
  }

  private void populateChildren(Route route, Map<RouteDescriptor, Route> ret) {
    for (RouteDescriptor child : children) {
      child.popupate(route, ret);
    }
  }
}
