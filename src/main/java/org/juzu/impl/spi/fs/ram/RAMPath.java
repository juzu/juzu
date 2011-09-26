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

package org.juzu.impl.spi.fs.ram;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RAMPath
{

   /** . */
   private final String name;

   /** . */
   private RAMDir parent;

   /** . */
   private long lastModified;

   public RAMPath()
   {
      this.name = "";
      this.parent = null;
      this.lastModified = System.currentTimeMillis();
   }

   public RAMPath(RAMDir parent, String name)
   {
      if (parent == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (name.length() == 0)
      {
         throw new IllegalArgumentException();
      }

      //
      this.name = name;
      this.parent = parent;
      this.lastModified = System.currentTimeMillis();
   }

   public String getName()
   {
      return name;
   }

   public RAMDir getParent()
   {
      return parent;
   }

   public long getLastModified()
   {
      return lastModified;
   }

   public void touch()
   {
      this.lastModified = System.currentTimeMillis();
   }

   public void remove()
   {
      if (name.length() == 0)
      {
         throw new UnsupportedOperationException("Cannot remove root file");
      }
      if (parent == null)
      {
         throw new IllegalStateException("Cannot remove removed file");
      }

      //
      parent.children.remove(name);
      parent = null;
   }
}
