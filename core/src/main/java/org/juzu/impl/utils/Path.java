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

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Path implements Iterable<String>
{
   
   public static Path parse(String s, char separator)
   {
      int index = 0;
      int from = 0;
      while (true)
      {
         int pos = s.indexOf(separator, from);
         if (pos == -1)
         {
            if (from < s.length())
            {
               index++;
            }
            break;
         }
         else
         {
            if (from < pos)
            {
               index++;
            }
            from = pos + 1;
         }
      }
      
      //
      String[] name = new String[index];

      //
      from = 0;
      index = 0;
      while (true)
      {
         int pos = s.indexOf(separator, from);
         if (pos == -1)
         {
            if (from < s.length())
            {
               name[index++] = s.substring(from);
            }
            break;
         }
         else
         {
            if (from < pos)
            {
               name[index++] = s.substring(from, pos);
            }
            from = pos + 1;
         }
      }

      //
      return new Path(name, 0);
   }
   
   /** . */
   private final String[] names;

   /** . */
   private final int from;

   /** . */
   private Path next;

   private Path(String names[], int from)
   {
      this.names = names;
      this.from = from;
      this.next = null;
   }
   
   public Path next()
   {
      if (from < names.length)
      {
         if (next == null)
         {
            next = new Path(names, from + 1);
         }
      }
      return next;
   }
   
   public int size()
   {
      return names.length - from;
   }
   
   public String get(int index) throws IndexOutOfBoundsException
   {
      return names[from + index];
   }

   public Iterator<String> iterator()
   {
      return Tools.iterator(from, names);
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("Path[");
      for (int i = from;i < names.length;i++)
      {
         if (i > from)
         {
            sb.append('.');
         }
         sb.append(names[i]);
      }
      sb.append(']');
      return sb.toString();
   }
}
