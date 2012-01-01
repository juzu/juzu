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

package org.juzu.metadata;

import org.juzu.request.Phase;
import org.juzu.impl.utils.Tools;

import javax.lang.model.element.VariableElement;
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
   private final String id;

   /** . */
   private final Phase phase;

   /** . */
   private final Class<?> type;

   /** . */
   private final Method method;

   /** . */
   private final List<ControllerParameter> argumentParameters;

   public ControllerMethod(
      String id,
      Phase phase,
      Class<?> type,
      Method method,
      List<ControllerParameter> argumentParameters)
   {
      if (id == null)
      {
         // For now we compute an id based on a kind of signature
         StringBuilder sb = new StringBuilder();
         sb.append(method.getDeclaringClass().getSimpleName());
         sb.append("_");
         sb.append(method.getName());
         for (ControllerParameter ve : argumentParameters)
         {
            sb.append("_").append(ve.getName());
         }
         id = sb.toString();
      }
      
      //
      this.id = id;
      this.phase = phase;
      this.type = type;
      this.method = method;
      this.argumentParameters = Tools.safeUnmodifiableList(argumentParameters);
   }

   public String getId()
   {
      return id;
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

   public String getName()
   {
      return method.getName();
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
