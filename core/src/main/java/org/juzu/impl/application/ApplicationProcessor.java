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

import org.juzu.Action;
import org.juzu.Application;
import org.juzu.Binding;
import org.juzu.Render;
import org.juzu.URLBuilder;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.application.Phase;
import org.juzu.application.PhaseLiteral;
import org.juzu.impl.utils.PackageMap;
import org.juzu.impl.utils.Tools;
import org.juzu.impl.request.ControllerParameter;
import org.juzu.impl.request.RenderContext;

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
import java.util.HashSet;
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

   /** . */
   private static final String PHASE_LITERAL = PhaseLiteral.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_METHOD = ControllerMethod.class.getSimpleName();

   /** . */
   private static final String PHASE = Phase.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_PARAMETER = ControllerParameter.class.getSimpleName();

   /** . */
   private static final String TOOLS = Tools.class.getSimpleName();

   /**
    * Application meta data.
    */
   static class ApplicationMetaData
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
      private final List<ControllerMetaData> controllers;

      ApplicationMetaData(PackageElement packageElt, String applicationName)
      {
         String packageName = packageElt.getQualifiedName().toString();

         //
         this.packageElt = packageElt;
         this.fqn = packageName + "." + applicationName;
         this.name = applicationName;
         this.packageName = packageName;
         this.prefix = packageName + ".";
         this.controllers = new ArrayList<ControllerMetaData>();
      }
   }

   /**
    * Controller meta data.
    */
   static class ControllerMetaData
   {

      /** . */
      private final TypeElement typeElt;

      /** . */
      private final List<MethodMetaData> methods;

      ControllerMetaData(TypeElement typeElt)
      {
         this.typeElt = typeElt;
         this.methods = new ArrayList<MethodMetaData>();
      }
   }

   static class MethodMetaData
   {

      /** . */
      private final Phase phase;

      /** . */
      private final ExecutableElement element;

      /** . */
      private final ExecutableType type;

      /** . */
      private final List<ControllerParameter> annotationParameters;

      MethodMetaData(Phase phase, ExecutableElement element, List<ControllerParameter> parameters)
      {
         this.phase = phase;
         this.element = element;
         this.type = (ExecutableType)element.asType();
         this.annotationParameters = parameters;
      }

      CharSequence getName()
      {
         return element.getSimpleName();
      }
   }

   private StringBuilder manifest = new StringBuilder();

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {


      Filer filer = processingEnv.getFiler();

      // Discover all applications
      PackageMap<ApplicationMetaData> applications = new PackageMap<ApplicationMetaData>();
      for (Element elt : roundEnv.getElementsAnnotatedWith(Application.class))
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
         applications.putValue(packageName, new ApplicationMetaData(packageElt, name));
      }

      // Collect controller methods
      Map<String, ControllerMetaData> controllerMap = new HashMap<String, ControllerMetaData>();
      Set<? extends Element> actions = roundEnv.getElementsAnnotatedWith(Action.class);
      Set<? extends Element> renders = roundEnv.getElementsAnnotatedWith(Render.class);
      Set<? extends Element> intersection = new HashSet<Element>(actions);
      intersection.retainAll(renders);
      if (intersection.size() > 0)
      {
         throw new UnsupportedOperationException("handle me gracefully " + renders);
      }

      //
      for (Set<? extends Element> elts : Arrays.asList(actions, renders))
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
               controllerMap.put(typeName, a = new ControllerMetaData(type));

               // Find the matching application
               PackageElement pkg = processingEnv.getElementUtils().getPackageOf(type);
               String fqn = pkg.getQualifiedName().toString();

               //
               ApplicationMetaData application = applications.resolveValue(fqn);
               if (application == null)
               {
                  throw new UnsupportedOperationException("handle me gracefully : could not find application for package " + fqn);
               }
               else
               {
                  application.controllers.add(a);
               }
            }

            //
            Phase phase;
            Binding[] bindings;
            if (elts == actions)
            {
               bindings = executableElt.getAnnotation(Action.class).parameters();
               phase = Phase.ACTION;
            }
            else
            {
               bindings = executableElt.getAnnotation(Render.class).parameters();
               phase = Phase.RENDER;
            }

            //
            List<ControllerParameter> parameters = new ArrayList<ControllerParameter>();
            for (Binding binding : bindings)
            {
               String value = binding.value().isEmpty() ? null : binding.value();
               parameters.add(new ControllerParameter(binding.name(), value));
            }

            //
            a.methods.add(new MethodMetaData(phase, executableElt, parameters));
         }
      }

      // Generate applications
      for (int i = 0;i < applications.getSize();i++)
      {
         ApplicationMetaData foo = applications.getValue(i);
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

               // Imports
               writer.append("import ").append(ApplicationDescriptor.class.getName()).append(";\n");
               writer.append("import ").append(PhaseLiteral.class.getName()).append(";\n");
               writer.append("import ").append(ControllerMethod.class.getName()).append(";\n");
               writer.append("import ").append(ControllerParameter.class.getName()).append(";\n");
               writer.append("import ").append(Tools.class.getName()).append(";\n");
               writer.append("import ").append(Arrays.class.getName()).append(";\n");
               writer.append("import ").append(Phase.class.getName()).append(";\n");
               writer.append("import ").append(URLBuilder.class.getName()).append(";\n");
               writer.append("import ").append(ApplicationContext.class.getName()).append(";\n");
               writer.append("import ").append(RenderContext.class.getName()).append(";\n");

               // Open class declaration
               writer.append("public class ").append(foo.name).append(" {\n");

               // Descriptor
               writer.append("public static final ApplicationDescriptor DESCRIPTOR = new ApplicationDescriptor(");
               writer.append("\"").append(foo.packageName).append("\",");
               writer.append("\"").append(foo.name).append("\",");
               writer.append("\"").append(templatesPackageName).append("\",");
               writer.append("Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
               for (ControllerMetaData bar : foo.controllers)
               {
                  for (Iterator<MethodMetaData> j = bar.methods.iterator();j.hasNext();)
                  {
                     MethodMetaData exe = j.next();
                     writer.append(bar.typeElt.getQualifiedName()).append("_").append(".").append(exe.getName()).append(".getDescriptor()");
                     if (j.hasNext())
                     {
                        writer.append(",");
                     }
                  }
               }
               writer.append("));\n");

               //
               for (ControllerMetaData controller : foo.controllers)
               {
                  int index = 0;
                  for (MethodMetaData method : controller.methods)
                  {
                     String controllerFQN = controller.typeElt.getQualifiedName().toString();

                     // Method
                     writer.append("private static final ").append(CONTROLLER_METHOD).append(" method_").append(String.valueOf(index)).append(" = ");
                     writer.append("new ").append(CONTROLLER_METHOD).append("(");
                     writer.append(PHASE).append(".").append(method.phase.name());
                     writer.append(",");
                     writer.append(controllerFQN).append(".class");
                     writer.append(",");
                     writer.append(TOOLS).append(".safeGetMethod(").append(controllerFQN).append(".class,\"").append(method.getName()).append("\"");
                     for (TypeMirror foobar : method.type.getParameterTypes())
                     {
                        TypeMirror erased = processingEnv.getTypeUtils().erasure(foobar);
                        writer.append(",").append(erased.toString()).append(".class");
                     }
                     writer.append(")");
                     writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                     for (Iterator<ControllerParameter> j = method.annotationParameters.iterator();j.hasNext();)
                     {
                        ControllerParameter boundParameter = j.next();
                        String name = "\"" + boundParameter.getName() + "\"";
                        String value = boundParameter.getValue() == null ? "null" : "\"" + boundParameter.getValue() + "\"";
                        writer.append("new ").append(CONTROLLER_PARAMETER).append("(").append(name).
                           append(",").append(value).append(")");
                        if (j.hasNext())
                        {
                           writer.append(",");
                        }
                     }
                     writer.append(")");
                     writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                     for (Iterator<? extends VariableElement> j = method.element.getParameters().iterator();j.hasNext();)
                     {
                        VariableElement ve = j.next();
                        writer.append("new ").append(CONTROLLER_PARAMETER).append("(\"").
                           append(ve.getSimpleName()).append("\")");
                        if (j.hasNext())
                        {
                           writer.append(",");
                        }
                     }
                     writer.append(")");
                     writer.append(");\n");

                     // URL builder
                     writer.append("public static URLBuilder ").append(method.getName()).append("URL").append("(");
                     List<? extends VariableElement> argDecls = method.element.getParameters();
                     List<? extends TypeMirror> argTypes = method.type.getParameterTypes();
                     for (int j = 0;j < argDecls.size();j++)
                     {
                        if (j > 0)
                        {
                           writer.append(',');
                        }
                        TypeMirror argumentType = argTypes.get(j);
                        VariableElement argDecl = argDecls.get(j);
                        writer.append(argumentType.toString()).append(" ").append(argDecl.getSimpleName().toString());
                     }
                     writer.append(") { return ((RenderContext)ApplicationContext.getCurrentRequest()).createURLBuilder(method_");
                     writer.append(Integer.toString(index));
                     switch (argDecls.size())
                     {
                        case 0:
                           break;
                        case 1:
                           writer.append(",(Object)").append(argDecls.get(0).getSimpleName());
                           break;
                        default:
                           writer.append(",new Object[]{");
                           for (int j = 0;j < argDecls.size();j++)
                           {
                              if (j > 0)
                              {
                                 writer.append(",");
                              }
                              VariableElement argDecl = argDecls.get(j);
                              writer.append(argDecl.getSimpleName());
                           }
                           writer.append("}");
                           break;
                     }
                     writer.append("); }\n");

                     //
                     index++;
                  }
               }

               // Close class declaration
               writer.append("}\n");
            }
            finally
            {
               Tools.safeClose(writer);
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
               Tools.safeClose(writer);
            }
         }
         catch (IOException e)
         {
            throw new UnsupportedOperationException("handle me gracefully", e);
         }
      }

      // Generate the action literals
      for (Map.Entry<String, ControllerMetaData> entry : controllerMap.entrySet())
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
               writer.append("import ").append(PhaseLiteral.class.getName()).append(";\n");
               writer.append("import ").append(ControllerMethod.class.getName()).append(";\n");
               writer.append("import ").append(ControllerParameter.class.getName()).append(";\n");
               writer.append("import ").append(Tools.class.getName()).append(";\n");
               writer.append("import ").append(Arrays.class.getName()).append(";\n");
               writer.append("import ").append(Phase.class.getName()).append(";\n");
               writer.append("import ").append(URLBuilder.class.getName()).append(";\n");
               writer.append("import ").append(ApplicationContext.class.getName()).append(";\n");
               writer.append("import ").append(RenderContext.class.getName()).append(";\n");
               writer.append("public class ").append(entry.getValue().typeElt.getSimpleName()).append("_ {\n");

               //
               int index = 0;
               for (MethodMetaData method : entry.getValue().methods)
               {
                  // Method
                  writer.append("private static final ").append(CONTROLLER_METHOD).append(" method_").append(String.valueOf(index)).append(" = ");
                  writer.append("new ").append(CONTROLLER_METHOD).append("(");
                  writer.append(PHASE).append(".").append(method.phase.name());
                  writer.append(",");
                  writer.append(entry.getKey()).append(".class");
                  writer.append(",");
                  writer.append(TOOLS).append(".safeGetMethod(").append(type).append(".class,\"").append(method.getName()).append("\"");
                  for (TypeMirror foobar : method.type.getParameterTypes())
                  {
                     TypeMirror erased = processingEnv.getTypeUtils().erasure(foobar);
                     writer.append(",").append(erased.toString()).append(".class");
                  }
                  writer.append(")");
                  writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                  for (Iterator<ControllerParameter> i = method.annotationParameters.iterator();i.hasNext();)
                  {
                     ControllerParameter boundParameter = i.next();
                     String name = "\"" + boundParameter.getName() + "\"";
                     String value = boundParameter.getValue() == null ? "null" : "\"" + boundParameter.getValue() + "\"";
                     writer.append("new ").append(CONTROLLER_PARAMETER).append("(").append(name).
                        append(",").append(value).append(")");
                     if (i.hasNext())
                     {
                        writer.append(",");
                     }
                  }
                  writer.append(")");
                  writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                  for (Iterator<? extends VariableElement> i = method.element.getParameters().iterator();i.hasNext();)
                  {
                     VariableElement ve = i.next();
                     writer.append("new ").append(CONTROLLER_PARAMETER).append("(\"").
                        append(ve.getSimpleName()).append("\")");
                     if (i.hasNext())
                     {
                        writer.append(",");
                     }
                  }
                  writer.append(")");
                  writer.append(");\n");

                  // Maybe remove that
                  writer.append("public static final ").append(PHASE_LITERAL).append(" ").append(method.getName()).append(" = ");
                  writer.append("new ").append(PHASE_LITERAL).append("(method_").append(Integer.toString(index)).append(")");
                  writer.append(";\n");

                  //
                  index++;
               }

               //
               writer.append("}\n");
            }
            finally
            {
               Tools.safeClose(writer);
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
