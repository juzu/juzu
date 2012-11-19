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

package juzu.impl.plugin.router.metamodel;

import juzu.Route;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;
import juzu.impl.metamodel.AnnotationState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMetaModel implements Serializable {

  /** . */
  static final FQN FQN = new FQN(Route.class);

  /** . */
  HashMap<String, String> targets;

  /** . */
  TreeMap<Integer, LinkedHashMap<String, RouteMetaModel>> children;

  /** . */
  HashMap<String, String> parameters;

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
      children = new TreeMap<Integer, LinkedHashMap<String, RouteMetaModel>>();
    }
    LinkedHashMap<String, RouteMetaModel> child = children.get(priority);
    if (child == null) {
      children.put(priority, child = new LinkedHashMap<String, RouteMetaModel>());
    }
    RouteMetaModel foo = child.get(path);
    if (foo == null) {
      child.put(path, foo = new RouteMetaModel());
    }
    foo.parameters = parameters;
    return foo;
  }

  public Map<String, String> getTargets() {
    return targets;
  }

  public Collection<Integer> getPriorities() {
    return children != null ? children.keySet() : null;
  }

  public Map<String, RouteMetaModel> getChildren(int priority) {
    return children != null ? children.get(priority) : null;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public JSON toJSON() {

    if (children != null) {
    }

    //
    JSON json = new JSON();

    //
    if (targets != null && targets.size() > 0) {
      json.set("targets", targets);
    }

    //
    if (children != null && children.size() > 0) {
      List<JSON> a = new LinkedList<JSON>();
      for (LinkedHashMap<String, RouteMetaModel> b : children.values()) {
        for (Map.Entry<String, RouteMetaModel> entry : b.entrySet()) {
          JSON foo = entry.getValue().toJSON();
          foo.set("path", entry.getKey());
          a.add(foo);
        }
      }
      json.set("routes", a);
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
    return json;
  }
}
