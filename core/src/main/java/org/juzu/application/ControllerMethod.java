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

package org.juzu.application;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A controller method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class ControllerMethod
{

   /** . */
   private final Phase phase;

   /** . */
   private final Class<?> type;

   /** . */
   private final Method method;

   /** . */
   private final List<String> names;

   public ControllerMethod(Phase phase, Class<?> type, Method method, String... names)
   {
      if (type == null)
      {
         throw new NullPointerException();
      }
      if (method == null)
      {
         throw new NullPointerException();
      }

      //
      this.phase = phase;
      this.type = type;
      this.method = method;
      this.names = Collections.unmodifiableList(Arrays.asList(names));
   }

   public Phase getPhase()
   {
      return phase;
   }

   public Class<?> getType()
   {
      return type;
   }

   public Method getMethod()
   {
      return method;
   }

   public String getMethodName()
   {
      return method.getName();
   }

   public List<String> getNames()
   {
      return names;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[type=" + type.getName() + ",method=" + method + "]";
   }

}
