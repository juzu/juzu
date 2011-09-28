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
import java.util.NoSuchElementException;

/**
 * An iterator that splits a string into chunks without requiring to allocate an array to hold
 * the various chunks of the splitted string.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Spliterator implements Iterator<String>
{

   /** . */
   private final String s;

   /** . */
   private final char separator;

   /** . */
   private int from;

   /** . */
   private Integer to;

   /**
    * Creates a spliterator.
    *
    * @param s the string to split
    * @param separator the separator
    * @throws NullPointerException if the string is null
    */
   public Spliterator(String s, char separator) throws NullPointerException
   {
      if (s == null)
      {
         throw new NullPointerException();
      }

      //
      this.s = s;
      this.separator = separator;
      this.from = 0;
      this.to = null;
   }

   public boolean hasNext()
   {
      if (from == -1)
      {
         return false;
      }
      else
      {
         if (to == null)
         {
            to = s.indexOf(separator, from);
         }
         return true;
      }
   }

   public String next()
   {
      if (hasNext())
      {
         String next;
         if (to == -1)
         {
            next = s.substring(from);
            from = -1;
         }
         else
         {
            next = s.substring(from, to);
            from = to + 1;
         }
         to = null;
         return next;
      }
      else
      {
         throw new NoSuchElementException();
      }
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
}
