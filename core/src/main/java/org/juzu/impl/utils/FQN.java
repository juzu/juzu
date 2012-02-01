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

package org.juzu.impl.utils;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FQN implements Serializable
{

   /** . */
   private final String fullName;

   /** . */
   private final QN packageName;

   /** . */
   private final String simpleName;

   public FQN(Class<?> type)
   {
      this(type.getName());
   }

   public FQN(String fullName)
   {
      QN packageName;
      String simpleName;
      int pos = fullName.lastIndexOf('.');
      if (pos == - 1)
      {
         packageName = new QN("");
         simpleName = fullName;
      }
      else
      {
         packageName = new QN(fullName.substring(0, pos));
         simpleName = fullName.substring(pos + 1);
      }

      //
      this.fullName = fullName;
      this.packageName = packageName;
      this.simpleName = simpleName;
   }

   public FQN(CharSequence packageName, String simpleName)
   {
      this(new QN(packageName), simpleName);
   }

   public FQN(QN packageName, String simpleName)
   {
      this.packageName = packageName;
      this.simpleName = simpleName;
      this.fullName = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
   }

   public String getFullName()
   {
      return fullName;
   }

   public QN getPackageName()
   {
      return packageName;
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   @Override
   public int hashCode()
   {
      return fullName.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof FQN)
      {
         FQN that = (FQN)obj;
         return packageName.equals(that.packageName) && simpleName.equals(that.simpleName);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return fullName;
   }
}
