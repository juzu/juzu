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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor
{

   /** . */
   private final String packageName;

   /** . */
   private final String name;

   /** . */
   private Class<?> defaultController;

   /** . */
   private final List<ControllerDescriptor> controllers;

   /** . */
   private final List<ControllerMethod> controllerMethods;

   /** . */
   private final String templatesPackageName;

   /** . */
   private final List<TemplateDescriptor> templates;

   public ApplicationDescriptor(
      String packageName,
      String name,
      Class<?> defaultController,
      String templatesPackageName,
      List<ControllerDescriptor> controllers,
      List<TemplateDescriptor> templates)
   {
      List<ControllerMethod> foo = new ArrayList<ControllerMethod>();
      for (ControllerDescriptor controller : controllers)
      {
         for (ControllerMethod method : controller.getMethods())
         {
            foo.add(method);
         }
      }

      //
      this.defaultController = defaultController;
      this.packageName = packageName;
      this.name = name;
      this.templatesPackageName = templatesPackageName;
      this.controllers = Collections.unmodifiableList(controllers);
      this.controllerMethods = Collections.unmodifiableList(foo);
      this.templates = Collections.unmodifiableList(templates);
   }

   public String getPackageName()
   {
      return packageName;
   }

   public String getName()
   {
      return name;
   }

   public Class<?> getDefaultController()
   {
      return defaultController;
   }

   public List<ControllerDescriptor> getControllers()
   {
      return controllers;
   }

   public List<ControllerMethod> getControllerMethods()
   {
      return controllerMethods;
   }

   public List<TemplateDescriptor> getTemplates()
   {
      return templates;
   }

   public ControllerMethod getControllerMethod(Class<?> type, String name, Class<?>... parameterTypes)
   {
      for (int i = 0;i < controllerMethods.size();i++)
      {
         ControllerMethod cm = controllerMethods.get(i);
         Method m = cm.getMethod();
         if (type.equals(cm.getType()) && m.getName().equals(name))
         {
            Class<?>[] a = m.getParameterTypes();
            if (a.length == parameterTypes.length)
            {
               for (int j = 0;j < parameterTypes.length;j++)
               {
                  if (!a[j].equals(parameterTypes[j]))
                  {
                     continue;
                  }
               }
               return cm;
            }
         }
      }
      return null;
   }

   public ControllerMethod getControllerMethodById(String methodId)
   {
      for (int i = 0;i < controllerMethods.size();i++)
      {
         ControllerMethod cm = controllerMethods.get(i);
         if (cm.getId().equals(methodId))
         {
            return cm;
         }
      }
      return null;
   }

   public String getTemplatesPackageName()
   {
      return templatesPackageName;
   }
}
