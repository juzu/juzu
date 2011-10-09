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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Safe
{

   public static boolean equals(Object o1, Object o2)
   {
      return o1 == null ? o2 == null : (o2 != null && o1.equals(o2));
   }

   public static void close(Closeable closeable)
   {
      if (closeable != null)
      {
         try
         {
            closeable.close();
         }
         catch (IOException ignore)
         {
         }
      }
   }

   public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes)
   {
      try
      {
         return type.getDeclaredMethod(name, parameterTypes);
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
   }

   public static <T> List<T> unmodifiableList(T... list)
   {
      return unmodifiableList(Arrays.asList(list));
   }

   public static <T> List<T> unmodifiableList(List<T> list)
   {
      if (list == null || list.isEmpty())
      {
         return Collections.emptyList();
      }
      else
      {
         return Collections.unmodifiableList(new ArrayList<T>(list));
      }
   }
}
