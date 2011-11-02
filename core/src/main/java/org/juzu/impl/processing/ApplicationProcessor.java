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

import org.juzu.Action;
import org.juzu.Application;
import org.juzu.Phase;
import org.juzu.Resource;
import org.juzu.View;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ProcessorPlugin;
import org.juzu.impl.utils.PackageMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Application processor.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationProcessor extends ProcessorPlugin
{

   /** . */
   final PackageMap<ApplicationMetaData> applications = new PackageMap<ApplicationMetaData>();

   /** The applications generated this round. */
   final Set<ApplicationMetaData> roundApplications = new LinkedHashSet<ApplicationMetaData>();

   public ApplicationMetaData getApplication(PackageElement packageElt)
   {
      return applications.resolveValue(packageElt.getQualifiedName().toString());
   }

   @Override
   public void process()
   {
      roundApplications.clear();

      // Discover all applications
      for (Element elt : getElementsAnnotatedWith(Application.class))
      {
         PackageElement packageElt = (PackageElement)elt;
         String packageName = packageElt.getQualifiedName().toString();

         // Check that we have no matching application for this package
         if (applications.resolveValue(packageName) != null)
         {
            throw new UnsupportedOperationException("handle me gracefully");
         }

         // Get data from Application annotation, the hard way
         String name = "";
         String defaultController = Object.class.getName();
         for (AnnotationMirror am : elt.getAnnotationMirrors())
         {
            TypeElement te = (TypeElement)am.getAnnotationType().asElement();
            if (te.getQualifiedName().toString().equals(Application.class.getName()))
            {
               for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet())
               {
                  String m = entry.getKey().getSimpleName().toString();
                  Object value = entry.getValue().getValue();
                  if ("name".equals(m))
                  {
                     name = (String)value;
                  }
                  else if ("defaultController".equals(m))
                  {
                     defaultController = value.toString();
                  }
               }
            }
         }

         //
         if (name.isEmpty())
         {
            name = packageElt.getSimpleName().toString();
            if (name.isEmpty())
            {
               throw new UnsupportedOperationException("handle me gracefully");
            }
            else
            {
               name = Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Application";
            }
         }
         ApplicationMetaData application = new ApplicationMetaData(packageElt, name, defaultController);
         applications.putValue(packageName, application);
         roundApplications.add(application);
      }

      // Collect controller methods
      Map<String, ControllerMetaData> controllerMap = new HashMap<String, ControllerMetaData>();
      Set<? extends Element> actions = getElementsAnnotatedWith(Action.class);
      Set<? extends Element> renders = getElementsAnnotatedWith(View.class);
      Set<? extends Element> resources = getElementsAnnotatedWith(Resource.class);

      //
      for (Set<? extends Element> elts : Arrays.asList(actions, renders, resources))
      {
         for (Element elt : elts)
         {
            ExecutableElement executableElt = (ExecutableElement)elt;

            // Find the matching type and the enclosing application
            TypeElement type = (TypeElement)executableElt.getEnclosingElement();

            //
            String typeName = type.getQualifiedName().toString();
            ControllerMetaData a = controllerMap.get(typeName);
            if (a == null)
            {
               // Find the matching application
               PackageElement pkg = getPackageOf(type);
               String fqn = pkg.getQualifiedName().toString();

               //
               ApplicationMetaData application = applications.resolveValue(fqn);
               if (application == null)
               {
                  throw new UnsupportedOperationException("handle me gracefully : could not find application for package " + fqn);
               }
               else
               {
                  controllerMap.put(typeName, a = new ControllerMetaData(type, application));
                  application.controllers.add(a);
               }
            }

            //
            Phase determined = null;
            String id = null;
            for (Phase phase : Phase.values())
            {
               Annotation annotation = elt.getAnnotation(phase.annotation);
               if (annotation != null)
               {
                  if (determined != null)
                  {
                     throw new CompilationException(elt, "Controller method cannot be involved in more than one phase : {" + determined + ", " + phase + "}");
                  }
                  determined = phase;
                  id = phase.id(annotation);
               }
            }

            //
            if (id.length() == 0)
            {
               id = "method_" + a.application.methodCount++;
            }
            else
            if (a.application.methodIds.contains(id))
            {
               throw new CompilationException(elt, "Duplicate controller method name " + id);
            }
            a.application.methodIds.add(id);

            //
            a.application.methodIds.add(id);
            a.methods.add(new MethodMetaData(a, id, determined, executableElt));
         }
      }
   }
}
