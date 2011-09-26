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

package org.juzu.impl.template.parser;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TextItem extends SectionItem
{

   /** . */
   private final String data;

   public TextItem(Location pos, String data)
   {
      super(pos);

      //
      if (data == null)
      {
         throw new NullPointerException();
      }

      //
      this.data = data;
   }

   public String getData()
   {
      return data;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof TextItem)
      {
         TextItem that = (TextItem)obj;
         return data.equals(that.data);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "DataText[pos=" + getPosition() + ",data=" + data + "]";
   }
}
