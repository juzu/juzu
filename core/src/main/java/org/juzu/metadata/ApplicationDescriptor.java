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

import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor
{

   /** . */
   private final Class<?> applicationClass;
   
   /** . */
   private final String packageName;

   /** . */
   private final String name;

   /** . */
   private Class<?> defaultController;

   /** . */
   private final Boolean escapeXML;

   /** . */
   private final List<ControllerDescriptor> controllers;

   /** . */
   private final List<ControllerMethod> controllerMethods;

   /** . */
   private final String templatesPackageName;

   /** . */
   private final List<TemplateDescriptor> templates;

   public ApplicationDescriptor(Class<?> applicationClass, Class<?> defaultController, Boolean escapeXML, String templatesPackageName)
   {
      // Load config
      JSON props;
      InputStream in = null;
      try
      {
         in = applicationClass.getResourceAsStream("config.json");
         String s = Tools.read(in);
         props = (JSON)JSON.parse(s);
      }
      catch (IOException e)
      {
         throw new AssertionError(e);
      }
      finally
      {
         Tools.safeClose(in);
      }

      //
      List<ControllerDescriptor> controllers = new ArrayList<ControllerDescriptor>();
      List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();
      List<TemplateDescriptor> templates = new ArrayList<TemplateDescriptor>();

      try
      {
         // Load controllers
         for (String fqn : props.getList("controllers", String.class))
         {
            Class<?> clazz = applicationClass.getClassLoader().loadClass(fqn);
            Field f = clazz.getField("DESCRIPTOR");
            ControllerDescriptor controller = (ControllerDescriptor)f.get(null);
            controllers.add(controller);
            controllerMethods.addAll(controller.getMethods());
         }

         // Load templates
         for (String fqn : props.getList("templates", String.class))
         {
            Class<?> clazz = applicationClass.getClassLoader().loadClass(fqn);
            Field f = clazz.getField("DESCRIPTOR");
            TemplateDescriptor descriptor = (TemplateDescriptor)f.get(null);
            templates.add(descriptor);
         }
      }
      catch (Exception e)
      {
         AssertionError ae = new AssertionError("Cannot load config");
         ae.initCause(e);
         throw ae;
      }

      //
      this.applicationClass = applicationClass;
      this.name = applicationClass.getSimpleName();
      this.packageName = applicationClass.getPackage().getName();
      this.templatesPackageName = templatesPackageName;
      this.defaultController = defaultController;
      this.escapeXML = escapeXML;
      this.controllers = controllers;
      this.controllerMethods = controllerMethods;
      this.templates = templates;
   }

   public Class<?> getApplicationClass()
   {
      return applicationClass;
   }
   
   public ClassLoader getApplicationLoader()
   {
      return applicationClass.getClassLoader();
   }

   public String getPackageName()
   {
      return packageName;
   }

   public Boolean getEscapeXML()
   {
      return escapeXML;
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
   
   public TemplateDescriptor getTemplate(String path) throws NullPointerException
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }
      for (TemplateDescriptor template : templates)
      {
         if (template.getPath().equals(path))
         {
            return template;
         }
      }
      return null;
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
