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

package org.juzu.impl.request;

import org.juzu.application.Phase;
import org.juzu.impl.utils.Safe;

import java.lang.reflect.Method;
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
   private final List<ControllerParameter> annotationParameters;

   /** . */
   private final List<ControllerParameter> argumentParameters;

   public ControllerMethod(
      Phase phase, Class<?> type,
      Method method,
      List<ControllerParameter> boundParameters,
      List<ControllerParameter> argumentParameters)
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
      this.annotationParameters = Safe.unmodifiableList(boundParameters);
      this.argumentParameters = Safe.unmodifiableList(argumentParameters);
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

   public List<ControllerParameter> getAnnotationParameters()
   {
      return annotationParameters;
   }

   public List<ControllerParameter> getArgumentParameters()
   {
      return argumentParameters;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[type=" + type.getName() + ",method=" + method + "]";
   }
}
