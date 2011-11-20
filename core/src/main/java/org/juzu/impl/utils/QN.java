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

/**
 * A qualified name.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class QN implements CharSequence, Serializable
{

   /** . */
   private final String value;

   public QN(CharSequence value) throws NullPointerException, IllegalArgumentException
   {
      if (value == null)
      {
         throw new NullPointerException();
      }
      if (value.length() > 0)
      {
         if (value.charAt(0) == '.')
         {
            throw new IllegalArgumentException("A qualified name cannot begin with '.' : " + value);
         }
         if (value.length() > 1)
         {
            if (value.charAt(value.length() - 1) == '.')
            {
               throw new IllegalArgumentException("A qualified name cannot end with '.' : " + value);
            }
            if (value.length() > 3)
            {
               char prev = value.charAt(1);
               for (int i = 2;i < value.length() - 1;i++)
               {
                  char next = value.charAt(i);
                  if (prev == '.' && next == '.')
                  {
                     throw new IllegalArgumentException("A qualified name cannot have two following '.' : " + value);
                  }
                  prev = next;
               }
            }
         }
      }

      //
      this.value = value.toString();
   }

   public String getValue()
   {
      return value;
   }

   public QN append(String simpleName) throws NullPointerException, IllegalArgumentException
   {
      if (simpleName == null)
      {
         throw new NullPointerException("No null simple name accepted");
      }
      if (simpleName.isEmpty())
      {
         throw new IllegalArgumentException("No empty simple name can be appended");
      }
      if (simpleName.indexOf('.') != -1)
      {
         throw new IllegalArgumentException("A simple name cannot contain a '.'");
      }
      if (value.isEmpty())
      {
         return new QN(simpleName);
      }
      else
      {
         return new QN(value + "." + simpleName);
      }
   }

   public boolean isEmpty()
   {
      return value.isEmpty();
   }

   public int length()
   {
      return value.length();
   }

   public char charAt(int index)
   {
      return value.charAt(index);
   }

   public CharSequence subSequence(int start, int end)
   {
      return value.subSequence(start, end);
   }

   @Override
   public String toString()
   {
      return value;
   }

   public boolean isPrefix(QN qn)
   {
      return qn.value.startsWith(value) && (qn.value.length() == value.length() || qn.value.charAt(qn.value.length()) == '.');
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof QN)
      {
         QN that = (QN)obj;
         return value.equals(that.value);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return value.hashCode();
   }
}
