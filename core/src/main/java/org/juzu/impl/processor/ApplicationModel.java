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

package org.juzu.impl.processor;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.utils.FQN;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationModel implements Serializable
{

   /** . */
   final ElementHandle origin;

   /** . */
   final FQN fqn;

   /** . */
   final String defaultController;

   /** . */
   final LinkedHashMap<String, ControllerModel> controllers;

   /** . */
   final String templatesFQN;

   /** . */
   final LinkedHashMap<String, TemplateModel> templates;

   public ApplicationModel(ElementHandle origin, FQN fqn, String defaultController, String templatesFQN)
   {
      this.origin = origin;
      this.defaultController = defaultController;
      this.fqn = fqn;
      this.controllers = new LinkedHashMap<String, ControllerModel>();
      this.templatesFQN = templatesFQN;
      this.templates = new LinkedHashMap<String, TemplateModel>();
   }

   public MethodModel resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException
   {
      TreeSet<MethodModel> set = new TreeSet<MethodModel>(
         new Comparator<MethodModel>()
         {
            public int compare(MethodModel o1, MethodModel o2)
            {
               return ((Integer)o1.parameterNames.size()).compareTo(o2.parameterNames.size());
            }
         }
      );
      for (ControllerModel controller : controllers.values())
      {
         for (MethodModel method : controller.methods)
         {
            if (typeName == null || controller.fqn.getSimpleName().equals(typeName))
            {
               if (method.name.equals(methodName) && method.parameterNames.containsAll(parameterNames))
               {
                  set.add(method);
               }
            }
         }
      }
      if (set.isEmpty())
      {
         return null;
      }
      else if (set.size() == 1)
      {
         return set.iterator().next();
      }
      else
      {
         return null;
      }
   }
}
