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
