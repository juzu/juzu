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

package juzu.impl.plugin.router.metamodel;

import juzu.Route;
import juzu.impl.common.JSON;
import juzu.impl.common.Name;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMetaModel implements Serializable {

  /** . */
  static final Name FQN = Name.create(Route.class);

  /** . */
  HashMap<String, String> targets;

  /** . */
  ArrayList<RouteMetaModel> children;

  /** . */
  final String path;

  /** . */
  final Integer priority;

  /** . */
  HashMap<String, String> parameters;

  public RouteMetaModel(String path, Integer priority) {
    this.path = path;
    this.priority = priority;
  }

  public String getPath() {
    return path;
  }

  /**
   * Sets a target if the target is not null otherwise remove it.
   *
   * @param key the key
   * @param target the target
   * @return this object
   * @throws NullPointerException if the key is null
   * @throws IllegalArgumentException if the key is already present
   */
  RouteMetaModel setTarget(String key, String target) throws NullPointerException, IllegalArgumentException {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }
    if (target == null) {
      if (targets != null) {
        targets.remove(key);
      }
    } else {
      if (targets == null) {
        targets = new HashMap<String, String>();
      }
      if (targets.containsKey(key)) {
        throw new IllegalArgumentException("Cannot have two identical targets " + key);
      } else {
        targets.put(key, target);
      }
    }
    return this;
  }

  /**
   * Returns a keyed target.
   *
   * @param key the key
   * @return the target or null
   * @throws NullPointerException if the key is null
   */
  String getTarget(String key) throws NullPointerException {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }
    if (targets != null) {
      return targets.get(key);
    } else {
      return null;
    }
  }

  RouteMetaModel addChild(int priority, String path, HashMap<String, String> parameters) {
    if (children == null) {
      children = new ArrayList<RouteMetaModel>();
    }
    RouteMetaModel found = null;
    for (RouteMetaModel child : children) {
      if (child.path.equals(path) && child.priority == priority) {
        found = child;
        break;
      }
    }
    if (found == null) {
      children.add(found = new RouteMetaModel(path, priority));
    }
    if (parameters != null && parameters.size() > 0) {
      if (found.parameters == null) {
        found.parameters = new LinkedHashMap<String, String>();
      }
      found.parameters.putAll(parameters);
    }
    return found;
  }

  public List<RouteMetaModel> getChildren() {
    return children;
  }

  public Map<String, String> getTargets() {
    return targets;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public JSON toJSON() {

    //
    JSON json = new JSON();

    //
    if (path != null) {
      json.set("path", path);
    }

    //
    if (targets != null && targets.size() > 0) {
      json.set("targets", targets);
    }

    //
    if (parameters != null && parameters.size() > 0) {
      JSON b = new JSON();
      for (Map.Entry<String, String> parameter : parameters.entrySet()) {
        b.set(parameter.getKey(), new JSON().set("pattern", parameter.getValue()));
      }
      json.set("parameters", b);
    }

    //
    if (children != null && children.size() > 0) {
      RouteMetaModel[] routes = children.toArray(new RouteMetaModel[children.size()]);
      Arrays.sort(routes, new Comparator<RouteMetaModel>() {
        public int compare(RouteMetaModel o1, RouteMetaModel o2) {
          return o1.priority.compareTo(o2.priority);
        }
      });
      List<JSON> a = new LinkedList<JSON>();
      for (RouteMetaModel route : routes) {
        a.add(route.toJSON());
      }
      json.set("routes", a);
    }

    //
    return json;
  }
}
