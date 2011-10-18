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

import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class PackageMap<V>
{

   /** . */
   private ArrayList<String> entriesNames;

   /** . */
   private ArrayList<V> entriesValues;

   public PackageMap()
   {
      this.entriesNames = new ArrayList<String>();
      this.entriesValues = new ArrayList<V>();
   }

   public int getSize()
   {
      return entriesNames.size();
   }

   public String getName(int index) throws IndexOutOfBoundsException
   {
      return entriesNames.get(index);
   }

   public V getValue(int index) throws IndexOutOfBoundsException
   {
      return entriesValues.get(index);
   }

   public V putValue(String packageName, V value) throws NullPointerException
   {
      if (packageName == null)
      {
         throw new NullPointerException("No null package name accepted");
      }
      if (value == null)
      {
         throw new NullPointerException("No null value accepted");
      }
      int size = entriesNames.size();
      for (int i = 0;i < size;i++)
      {
         String entryName = entriesNames.get(i);
         if (packageName.equals(entryName))
         {
            V previous = entriesValues.get(i);
            entriesValues.set(i, value);
            return previous;
         }
      }
      entriesNames.add(packageName);
      entriesValues.add(value);
      return null;
   }

   public V getValue(String packageName) throws NullPointerException
   {
      if (packageName == null)
      {
         throw new NullPointerException("No null package name accepted");
      }
      int size = entriesNames.size();
      for (int i = 0;i < size;i++)
      {
         String entryName = entriesNames.get(i);
         if (packageName.equals(entryName))
         {
            return entriesValues.get(i);
         }
      }
      return null;
   }

   public V resolveValue(String packageName) throws NullPointerException
   {
      if (packageName == null)
      {
         throw new NullPointerException("No null package name accepted");
      }
      int matchLength = 0;
      int matchIndex = -1;
      int size = entriesNames.size();
      for (int i = 0;i < size;i++)
      {
         String entryName = entriesNames.get(i);
         if (packageName.equals(entryName))
         {
            matchLength = entryName.length();
            matchIndex = i;
            break;
         }
         else if (matchLength == 0 && entryName.length() == 0)
         {
            matchLength = 0;
            matchIndex = i;
         }
         else if (
            entryName.length() > matchLength &&
            packageName.length() > entryName.length() + 1 &&
            packageName.charAt(entryName.length()) == '.' &&
            packageName.startsWith(entryName))
         {
            matchLength = entryName.length();
            matchIndex = i;
         }
      }
      return matchIndex != -1 ? entriesValues.get(matchIndex) : null;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("PackageMap[");
      int size = entriesNames.size();
      for (int i = 0;i < size;i++)
      {
         if (i > 0)
         {
            sb.append(',');
         }
         sb.append(entriesNames.get(i));
         sb.append('=');
         sb.append(entriesValues.get(i));
      }
      sb.append("]");
      return sb.toString();
   }
}
