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

package org.juzu.impl.metamodel;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelEvent implements Serializable
{

   public static class AddObject extends MetaModelEvent
   {

      /** . */
      private final MetaModelObject object;

      public AddObject(MetaModelObject object)
      {
         this.object = object;
      }

      public MetaModelObject getObject()
      {
         return object;
      }

      public String toString()
      {
         return getClass().getSimpleName() + "[" + object.getClass().getSimpleName() + "]";
      }
   }

   public static class RemoveObject extends MetaModelEvent
   {

      /** . */
      private final MetaModelObject object;

      public RemoveObject(MetaModelObject object)
      {
         this.object = object;
      }

      public MetaModelObject getObject()
      {
         return object;
      }

      public String toString()
      {
         return getClass().getSimpleName() + "[" + object.getClass().getSimpleName() + "]";
      }
   }

   /**
    * A directed relationship between two meta model objects.
    */
   public static class AddRelationship extends MetaModelEvent
   {

      /** . */
      private final MetaModelObject source;

      /** . */
      private final MetaModelObject target;

      public AddRelationship(MetaModelObject source, MetaModelObject target)
      {
         this.source = source;
         this.target = target;
      }

      public MetaModelObject getSource()
      {
         return source;
      }

      public MetaModelObject getTarget()
      {
         return target;
      }

      public String toString()
      {
         return getClass().getSimpleName() + "[source=" + source.getClass().getSimpleName() + ",target=" + target.getClass().getSimpleName() + "]";
      }
   }

   /**
    * A directed relationship between two meta model objects.
    */
   public static class RemoveRelationship extends MetaModelEvent
   {

      /** . */
      private final MetaModelObject source;

      /** . */
      private final MetaModelObject target;

      public RemoveRelationship(MetaModelObject source, MetaModelObject target)
      {
         this.source = source;
         this.target = target;
      }

      public MetaModelObject getSource()
      {
         return source;
      }

      public MetaModelObject getTarget()
      {
         return target;
      }

      public String toString()
      {
         return getClass().getSimpleName() + "[source=" + source.getClass().getSimpleName() + ",target=" + target.getClass().getSimpleName() + "]";
      }
   }
}
