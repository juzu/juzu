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

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelEvent implements Serializable
{

   /** . */
   public static final int AFTER_ADD = 0;

   /** . */
   public static final int BEFORE_REMOVE = 1;

   /** . */
   public static final int UPDATED = 2;

   /** . */
   private final int type;

   /** . */
   private final MetaModelObject object;

   MetaModelEvent(int type, MetaModelObject object)
   {
      if (type < 0 || type > 3)
      {
         throw new IllegalArgumentException();
      }
      if (object == null)
      {
         throw new NullPointerException();
      }

      //
      this.type = type;
      this.object = object;
   }

   public int getType()
   {
      return type;
   }

   public MetaModelObject getObject()
   {
      return object;
   }

   public String toString()
   {
      return getClass().getSimpleName() + "[type=" + type + ",object=" + object.getClass().getSimpleName() + "]";
   }
}
