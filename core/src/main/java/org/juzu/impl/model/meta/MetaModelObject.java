/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.model.meta;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.utils.JSON;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelObject implements Serializable
{


   /** The children. */
   private final HashMap<Key<?>, Reference<MetaModelObject>> children = new HashMap<Key<?>, Reference<MetaModelObject>>();

   /** The parents. */
   private final HashMap<MetaModelObject, Reference<Key<?>>> parents = new HashMap<MetaModelObject, Reference<Key<?>>>();

   public final Collection<MetaModelObject> getParents()
   {
      return parents.keySet();
   }

   public final <O extends MetaModelObject> O getChild(Key<O> key)
   {
      Reference<MetaModelObject> child = children.get(key);
      if (child != null)
      {
         return key.getType().cast(child.target);
      }
      else
      {
         return null;
      }
   }

   public final Collection<MetaModelObject> getChildren()
   {
      return getChildren(MetaModelObject.class);
   }

   public final <O extends MetaModelObject> Collection<Key<O>> getKeys(Class<O> filter)
   {
      ArrayList<Key<O>> list = new ArrayList<Key<O>>(children.size());
      for (Key<?> key : children.keySet())
      {
         if (filter.isInstance(key.getType()))
         {
            // Yes : not great
            list.add((Key<O>)key);
         }
      }
      return list;
   }

   public final <O extends MetaModelObject> Collection<O> getChildren(Class<O> filter)
   {
      ArrayList<O> list = new ArrayList<O>(children.size());
      for (Reference<MetaModelObject> child : children.values())
      {
         if (filter.isInstance(child.target))
         {
            list.add(filter.cast(child.target));
         }
      }
      return list;
   }

   public final <O extends MetaModelObject> O addChild(Key<O> key, O child) throws IllegalArgumentException, IllegalStateException
   {
      return addChild(key, child, true);
   }

   public final <O extends MetaModelObject> O addChild(Key<O> key, O child, boolean strong) throws IllegalArgumentException, IllegalStateException
   {
      if (closure(child, new HashSet<MetaModelObject>()).contains(this))
      {
         throw new IllegalStateException("Cycle detected");
      }
      else
      {
         if (child.parents.containsKey(this))
         {
            throw new IllegalArgumentException("Child cannot be added");
         }
         else
         {
            children.put(key, new Reference<MetaModelObject>(child, strong));
            child.parents.put(this, new Reference<Key<?>>(key, strong));

            // Callback
            child.postAttach(this);
            return child;
         }
      }
   }

   public final <O extends MetaModelObject> O removeChild(Key<O> key)
   {
      Reference<MetaModelObject> child = children.remove(key);
      if (child != null)
      {
         if (child.target.parents.remove(this) != null)
         {
            child.target.preDetach(this);
            
            // Check if it's not strongly referenced anymore
            boolean remove = true;
            for (Reference<Key<?>> parent : child.target.parents.values())
            {
               if (parent.strong)
               {
                  remove = false;
               }
            }

            // Remove orphan
            if (remove)
            {
               // Remove from parents (they must all be weakly referenced)
               while (child.target.parents.size() > 0)
               {
                  Iterator<Map.Entry<MetaModelObject, Reference<Key<?>>>> iterator = child.target.parents.entrySet().iterator();
                  Map.Entry<MetaModelObject, Reference<Key<?>>> entry = iterator.next();
                  if (entry.getKey().children.remove(entry.getValue().target) == null)
                  {
                     throw new AssertionError();
                  }
                  else
                  {
                     iterator.remove();
                  }
               }

               // Remove children if needed
               if (child.target.children.size() > 0)
               {
                  for (Key<?> key2 : new ArrayList<Key<?>>(child.target.children.keySet()))
                  {
                     child.target.removeChild(key2);
                  }
               }

               // Remove callback
               child.target.preRemove();
            }

            //
            return key.getType().cast(child.target);
         }
         else
         {
            throw new AssertionError("Internal bug");
         }
      }
      else
      {
         throw new IllegalArgumentException("The element is not child of this node");
      }
   }

   public final void remove()
   {
      // We remove a node by detaching it from all its parents
      while (parents.size() > 0)
      {
         Iterator<Map.Entry<MetaModelObject, Reference<Key<?>>>> iterator = parents.entrySet().iterator();
         Map.Entry<MetaModelObject, Reference<Key<?>>> entry = iterator.next();
         MetaModelObject parent = entry.getKey();
         Reference<Key<?>> key = entry.getValue();
         parent.removeChild(key.target);
      }
   }

   protected void preDetach(MetaModelObject parent)
   {
   }

   protected void postAttach(MetaModelObject parent)
   {
   }

   protected void preRemove()
   {
   }

   /**
    * Check the existence of the object, the default implementation always return true.
    *
    * @return the existence
    * @param model
    */
   public boolean exist(MetaModel model)
   {
      return true;
   }

   public JSON toJSON()
   {
      return new JSON();
   }

   public void postActivate(MetaModel model)
   {
   }

   public void postProcess(MetaModel model) throws CompilationException
   {
   }

   public void prePassivate(MetaModel model)
   {
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[" + toJSON() + "]";
   }

   private Set<MetaModelObject> closure(MetaModelObject node, Set<MetaModelObject> closure)
   {
      if (!closure.contains(node))
      {
         closure.add(node);
         for (Reference<MetaModelObject> child : node.children.values())
         {
            closure(child.target, closure);
         }
      }
      return closure;
   }
}
