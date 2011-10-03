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

package org.juzu.impl.application;

import org.juzu.Application;
import org.juzu.Render;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.ControllerMethod;
import org.juzu.application.Phase;
import org.juzu.application.RenderLiteral;
import org.juzu.impl.utils.PackageMap;
import org.juzu.impl.utils.Safe;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Application processor.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@javax.annotation.processing.SupportedAnnotationTypes({"org.juzu.Application"})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class ApplicationProcessor extends AbstractProcessor
{

   static class Foo
   {

      /** . */
      private final PackageElement packageElt;

      /** . */
      private final String fqn;

      /** . */
      private final String prefix;

      /** . */
      private final String name;

      /** . */
      private final String packageName;

      /** . */
      private final List<Bar> renders;

      Foo(PackageElement packageElt, String applicationName)
      {
         String packageName = packageElt.getQualifiedName().toString();

         //
         this.packageElt = packageElt;
         this.fqn = packageName + "." + applicationName;
         this.name = applicationName;
         this.packageName = packageName;
         this.prefix = packageName + ".";
         this.renders = new ArrayList<Bar>();
      }
   }

   static class Bar
   {

      /** . */
      private final TypeElement typeElt;

      /** . */
      private final List<ExecutableElement> methods;

      Bar(TypeElement typeElt)
      {
         this.typeElt = typeElt;
         this.methods = new ArrayList<ExecutableElement>();
      }
   }

   private StringBuilder manifest = new StringBuilder();

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      Set<? extends Element> applicationAnns = roundEnv.getElementsAnnotatedWith(Application.class);


      Filer filer = processingEnv.getFiler();

      //
      PackageMap<Foo> applications = new PackageMap<Foo>();

      // Discover all applications
      for (Element elt : applicationAnns)
      {
         PackageElement packageElt = (PackageElement)elt;
         String packageName = packageElt.getQualifiedName().toString();

         // Check taht we have no matching application for this package
         if (applications.resolveValue(packageName) != null)
         {
            throw new UnsupportedOperationException("handle me gracefully");
         }

         //
         Application applicationAnn = elt.getAnnotation(Application.class);
         String name = applicationAnn.name();
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
         applications.putValue(packageName, new Foo(packageElt, name));
      }

      // Collect @Render
      Map<String, Bar> renderMap = new HashMap<String, Bar>();
      for (Element elt : roundEnv.getElementsAnnotatedWith(Render.class))
      {
         ExecutableElement executableElt = (ExecutableElement)elt;

         // Find the matching type and the enclosing application
         TypeElement type = (TypeElement)executableElt.getEnclosingElement();

         //
         String typeName = type.getQualifiedName().toString();
         Bar a = renderMap.get(typeName);
         if (a == null)
         {
            renderMap.put(typeName, a = new Bar(type));

            // Find the matching application
            PackageElement pkg = processingEnv.getElementUtils().getPackageOf(type);
            String fqn = pkg.getQualifiedName().toString();

            //
            Foo found = applications.resolveValue(fqn);
            if (found == null)
            {
               throw new UnsupportedOperationException("handle me gracefully : could not find application for package " + fqn);
            }
            else
            {
               found.renders.add(a);
            }
         }

         //
         a.methods.add(executableElt);
      }

      //
      for (int i = 0;i < applications.getSize();i++)
      {
         Foo foo = applications.getValue(i);
         try
         {
            JavaFileObject jfo = filer.createSourceFile(foo.fqn, foo.packageElt);
            Writer writer = jfo.openWriter();
            try
            {
               String templatesPackageName = foo.packageName;
               if (templatesPackageName.length() == 0)
               {
                  templatesPackageName = "templates";
               }
               else
               {
                  templatesPackageName += ".templates";
               }

               writer.append("package ").append(foo.packageElt.getQualifiedName()).append(";\n");
               writer.append("import ").append(ApplicationDescriptor.class.getName()).append(";\n");
               writer.append("import ").append(ControllerMethod.class.getName()).append(";\n");
               writer.append("import ").append(Arrays.class.getName()).append(";\n");
               writer.append("public class ").append(foo.name).append(" {\n");
               writer.append("public static final ApplicationDescriptor DESCRIPTOR = new ApplicationDescriptor(");
               writer.append("\"").append(foo.packageName).append("\",");
               writer.append("\"").append(foo.name).append("\",");
               writer.append("\"").append(templatesPackageName).append("\",");
               writer.append("Arrays.<").append(ControllerMethod.class.getSimpleName()).append(">asList(");
               for (Bar bar : foo.renders)
               {
                  for (Iterator<ExecutableElement> j = bar.methods.iterator();j.hasNext();)
                  {
                     ExecutableElement exe = j.next();
                     writer.append(bar.typeElt.getQualifiedName()).append("_").append(".").append(exe.getSimpleName()).append(".getDescriptor()");
                     if (j.hasNext())
                     {
                        writer.append(",");
                     }
                  }
               }

               writer.append("));\n");
               writer.append("}\n");
            }
            finally
            {
               Safe.close(writer);
            }
         }
         catch (IOException e)
         {
            throw new UnsupportedOperationException("handle me gracefully", e);
         }

         //
         manifest.append(foo.name).append('=').append(foo.fqn).append("\n");
      }

      //
      if (roundEnv.processingOver())
      {
         try
         {
            FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "org.juzu", "config.properties");
            Writer writer = fo.openWriter();
            try
            {
               writer.append(manifest);
            }
            finally
            {
               Safe.close(writer);
            }
         }
         catch (IOException e)
         {
            throw new UnsupportedOperationException("handle me gracefully", e);
         }
      }

      // Generate the action literals
      for (Map.Entry<String, Bar> entry : renderMap.entrySet())
      {
         try
         {
            String type = entry.getKey();
            JavaFileObject jfo = filer.createSourceFile(type + "_");
            Writer writer = jfo.openWriter();
            try
            {
               PackageElement pkg = processingEnv.getElementUtils().getPackageOf(entry.getValue().typeElt);
               writer.append("package ").append(pkg.getQualifiedName()).append(";\n");
               writer.append("import ").append(RenderLiteral.class.getName()).append(";\n");
               writer.append("import ").append(ControllerMethod.class.getName()).append(";\n");
               writer.append("import ").append(Safe.class.getName()).append(";\n");
               writer.append("import ").append(Phase.class.getName()).append(";\n");
               writer.append("public class ").append(entry.getValue().typeElt.getSimpleName()).append("_ {\n");

               //
               for (ExecutableElement executableElt : entry.getValue().methods)
               {
                  ExecutableType executableType = (ExecutableType)executableElt.asType();

                  //
                  writer.append("public static final RenderLiteral ").append(executableElt.getSimpleName()).append(" = ");
                  writer.append("new RenderLiteral(new ").append(ControllerMethod.class.getSimpleName()).append("(");

                  // Phase
                  writer.append(Phase.class.getSimpleName()).append(".").append(Phase.RENDER.name());
                  writer.append(",");

                  // Type
                  writer.append(entry.getKey()).append(".class");
                  writer.append(",");

                  // Method
                  writer.append("Safe.getMethod(").append(type).append(".class,\"").append(executableElt.getSimpleName()).append("\"");
                  for (TypeMirror foo : executableType.getParameterTypes())
                  {
                     TypeMirror erased = processingEnv.getTypeUtils().erasure(foo);
                     writer.append(",").append(erased.toString()).append(".class");
                  }
                  writer.append(")");

                  // Variable names
                  for (VariableElement ve : executableElt.getParameters())
                  {
                     writer.append(",\"").append(ve.getSimpleName()).append("\"");
                  }

                  //
                  writer.append("))");
                  writer.append(";\n");
               }
               writer.append("}\n");
            }
            finally
            {
               Safe.close(writer);
            }
         }
         catch (IOException e)
         {
            throw new UnsupportedOperationException("handle me gracefully", e);
         }
      }

      //
      return true;
   }
}
