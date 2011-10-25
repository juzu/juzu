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

package org.juzu.impl.processing;

import org.juzu.AmbiguousResolutionException;

import javax.lang.model.element.PackageElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Application meta data.
 */
class ApplicationMetaData
{

   /** . */
   final PackageElement packageElt;

   /** . */
   final String className;

   /** . */
   final String name;

   /** . */
   final String packageName;

   /** . */
   final String defaultController;

   /** . */
   int methodCount;

   /** . */
   final Set<String> methodIds;

   /** . */
   final List<ControllerMetaData> controllers;

   /** . */
   final List<TemplateMetaData> templates;

   ApplicationMetaData(PackageElement packageElt, String applicationName, String defaultController)
   {
      String packageName = packageElt.getQualifiedName().toString();

      //
      this.packageElt = packageElt;
      this.className = packageName + "." + applicationName;
      this.name = applicationName;
      this.packageName = packageName;
      this.defaultController = defaultController;
      this.methodIds = new HashSet<String>();
      this.controllers = new ArrayList<ControllerMetaData>();
      this.methodCount = 0;
      this.templates = new ArrayList<TemplateMetaData>();
   }

   public MethodMetaData resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException
   {
      TreeSet<MethodMetaData> set = new TreeSet<MethodMetaData>(
         new Comparator<MethodMetaData>()
         {
            public int compare(MethodMetaData o1, MethodMetaData o2)
            {
               return ((Integer)o1.parameterNames.size()).compareTo(o2.parameterNames.size());
            }
         }
      );
      for (ControllerMetaData controller : controllers)
      {
         for (MethodMetaData method : controller.methods)
         {
            if (typeName == null || controller.typeElt.getSimpleName().toString().equals(typeName))
            {
               if (method.getName().equals(methodName) && method.parameterNames.containsAll(parameterNames))
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
         throw new AmbiguousResolutionException();
      }
   }
}
