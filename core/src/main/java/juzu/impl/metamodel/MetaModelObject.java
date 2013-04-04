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

package juzu.impl.metamodel;

import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelObject implements Serializable {

  /** The children. */
  private final HashMap<Key<?>, MetaModelObject> children = new HashMap<Key<?>, MetaModelObject>();

  /** The parents. */
  private final HashMap<MetaModelObject, Key<?>> parents = new HashMap<MetaModelObject, Key<?>>();

  /** . */
  protected MetaModel metaModel;

  public MetaModelObject() {
  }

  public final Collection<MetaModelObject> getParents() {
    return parents.keySet();
  }

  public final <O extends MetaModelObject> O getChild(Key<O> key) {
    MetaModelObject child = children.get(key);
    if (child != null) {
      return key.getType().cast(child);
    }
    else {
      return null;
    }
  }

  public final Collection<MetaModelObject> getChildren() {
    return getChildren(MetaModelObject.class);
  }

  public final <O extends MetaModelObject> Collection<Key<O>> getKeys(Class<O> filter) {
    ArrayList<Key<O>> list = new ArrayList<Key<O>>(children.size());
    for (Key<?> key : children.keySet()) {
      if (filter.isAssignableFrom(key.getType())) {
        // Yes : not great
        list.add((Key<O>)key);
      }
    }
    return list;
  }

  public final <O extends MetaModelObject> Collection<O> getChildren(Class<O> filter) {
    ArrayList<O> list = new ArrayList<O>(children.size());
    for (MetaModelObject child : children.values()) {
      if (filter.isInstance(child)) {
        list.add(filter.cast(child));
      }
    }
    return list;
  }

  public final <O extends MetaModelObject> O addChild(Key<O> key, O child) throws IllegalArgumentException, IllegalStateException {
    return (O)_addChild(key, child);
  }

  private MetaModelObject _addChild(Key key, MetaModelObject child) throws IllegalArgumentException, IllegalStateException {
    if (closure(child, new HashSet<MetaModelObject>()).contains(this)) {
      throw new IllegalStateException("Cycle detected");
    }
    else {
      if (child.parents.containsKey(this)) {
        throw new IllegalArgumentException("Child " + child + " cannot be added for key " + key + " because parent already contains it");
      } else {
        if (children.containsKey(key)) {
          throw new IllegalArgumentException("Object " + this + " has already a child named "  + key);
        }

        if (child.parents.size() == 0) {

          // Context
          if (this instanceof MetaModel) {
            child.metaModel = (MetaModel)this;
          } else {
            child.metaModel = metaModel;
          }

          // Post construct
          child.postConstruct();
        }

        // Wire
        children.put(key, child);
        child.parents.put(this, key);


        // Post attach
        child.postAttach(this);

        //
        return child;
      }
    }
  }

  public final <O extends MetaModelObject> O removeChild(Key<O> key) {
    MetaModelObject child = children.get(key);
    if (child != null) {
      if (child.parents.containsKey(this)) {

        // Detect orphan
        boolean remove = child.parents.size() == 1;

        //
        if (remove) {

          // Remove children recursively
          if (child.children.size() > 0) {
            for (Key<?> key2 : new ArrayList<Key<?>>(child.children.keySet())) {
              child.removeChild(key2);
            }
          }
        }

        // Pre detach
        child.preDetach(this);

        // Break relationship
        if (children.remove(key) == null) {
          throw new AssertionError("Internal bug");
        }
        if (child.parents.remove(this) == null) {
          throw new AssertionError("Internal bug");
        }

        //
        if (remove) {
          // Remove callback
          child.preRemove();

          // Set model to null
          child.metaModel = null;
        }

        //
        return key.getType().cast(child);
      }
      else {
        throw new AssertionError("Internal bug");
      }
    }
    else {
      throw new IllegalArgumentException("The element is not child of this node");
    }
  }

  public final void remove() {
    // We remove a node by detaching it from all its parents
    while (parents.size() > 0) {
      Iterator<Map.Entry<MetaModelObject, Key<?>>> iterator = parents.entrySet().iterator();
      Map.Entry<MetaModelObject, Key<?>> entry = iterator.next();
      MetaModelObject parent = entry.getKey();
      Key<?> key = entry.getValue();
      parent.removeChild(key);
    }
  }

  public void queue(MetaModelEvent event) {
    metaModel.queue(event);
  }

  protected void postConstruct() {
  }

  protected void preDetach(MetaModelObject parent) {
  }

  protected void postAttach(MetaModelObject parent) {
  }

  protected void preRemove() {
  }

  public JSON toJSON() {
    return new JSON();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + toJSON() + "]";
  }

  private Set<MetaModelObject> closure(MetaModelObject node, Set<MetaModelObject> closure) {
    if (!closure.contains(node)) {
      closure.add(node);
      for (MetaModelObject child : node.children.values()) {
        closure(child, closure);
      }
    }
    return closure;
  }
}
