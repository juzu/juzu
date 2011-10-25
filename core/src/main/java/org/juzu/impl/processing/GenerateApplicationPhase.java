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

import org.juzu.Phase;
import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ProcessorPlugin;
import org.juzu.impl.utils.Tools;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.metadata.TemplateDescriptor;
import org.juzu.request.ActionContext;
import org.juzu.request.ApplicationContext;
import org.juzu.request.MimeContext;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GenerateApplicationPhase extends ProcessorPlugin
{

   /** . */
   static final String TEMPLATE_DESCRIPTOR = TemplateDescriptor.class.getSimpleName();

   /** . */
   static final String CONTROLLER_DESCRIPTOR = ControllerDescriptor.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_METHOD = ControllerMethod.class.getSimpleName();

   /** . */
   private static final String PHASE = Phase.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_PARAMETER = ControllerParameter.class.getSimpleName();

   /** . */
   private static final String TOOLS = Tools.class.getSimpleName();

   /** . */
   private static final String RESPONSE = Response.Render.class.getSimpleName();

   /** . */
   private StringBuilder manifest = new StringBuilder();

   @Override
   public void process() throws CompilationException
   {
      ApplicationProcessor processor = getPlugin(ApplicationProcessor.class);

      // Generate applications
      for (ApplicationMetaData foo : processor.roundApplications)
      {
         try
         {
            JavaFileObject jfo = createSourceFile(foo.className, foo.packageElt);
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
               writer.append("import ").append(Tools.getImport(ApplicationDescriptor.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(ControllerMethod.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(ControllerParameter.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(Tools.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(Arrays.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(Phase.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(URLBuilder.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(ApplicationContext.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(MimeContext.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(TemplateDescriptor.class)).append(";\n");
               writer.append("import ").append(Tools.getImport(ControllerDescriptor.class)).append(";\n");

               // Open class declaration
               writer.append("public class ").append(foo.name).append(" {\n");

               //
               int controllerIndex = 0;
               for (ControllerMetaData controller : foo.controllers)
               {
                  PackageElement fooElt = this.getPackageOf(controller.typeElt);
                  JavaFileObject jfo2 = createSourceFile(controller.typeElt.getQualifiedName() + "_");
                  Writer writer2 = jfo2.openWriter();

                  //
                  writer2.append("package ").append(fooElt.getQualifiedName()).append(";\n");
                  writer2.append("import ").append(Tools.getImport(ControllerMethod.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(ControllerParameter.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(Tools.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(Arrays.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(Phase.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(URLBuilder.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(InternalApplicationContext.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(MimeContext.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(ActionContext.class)).append(";\n");
                  writer2.append("import ").append(Tools.getImport(Response.Render.class)).append(";\n");
                  writer2.append("import ").append(foo.className).append(";\n");
                  writer2.append("public class ").append(controller.typeElt.getSimpleName()).append("_ {\n");

                  //
                  try
                  {
                     for (MethodMetaData method : controller.methods)
                     {
                        String controllerFQN = controller.typeElt.getQualifiedName().toString();

                        // Method
                        writer.append("public static final ").append(CONTROLLER_METHOD).append(" ").append(method.id).append(" = ");
                        writer.append("new ").append(CONTROLLER_METHOD).append("(");
                        writer.append("\"").append(method.id).append("\",");
                        writer.append(PHASE).append(".").append(method.phase.name()).append(",");
                        writer.append(controllerFQN).append(".class").append(",");
                        writer.append(TOOLS).append(".safeGetMethod(").append(controllerFQN).append(".class,\"").append(method.getName()).append("\"");
                        for (TypeMirror foobar : method.type.getParameterTypes())
                        {
                           TypeMirror erased = erasure(foobar);
                           writer.append(",").append(erased.toString()).append(".class");
                        }
                        writer.append(")");
                        writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                        for (Iterator<? extends VariableElement> j = method.element.getParameters().iterator(); j.hasNext(); )
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

                        //
                        List<? extends VariableElement> argDecls = method.element.getParameters();
                        List<? extends TypeMirror> argTypes = method.type.getParameterTypes();

                        // Response literal
                        if (method.phase == Phase.RENDER)
                        {
                           writer2.append("public static ").append(RESPONSE).append(" ").append(method.getName()).append("(");
                           for (int j = 0; j < argDecls.size(); j++)
                           {
                              if (j > 0)
                              {
                                 writer2.append(',');
                              }
                              TypeMirror argumentType = argTypes.get(j);
                              VariableElement argDecl = argDecls.get(j);
                              writer2.append(argumentType.toString()).append(" ").append(argDecl.getSimpleName().toString());
                           }
                           writer2.append(") { return ((ActionContext)InternalApplicationContext.getCurrentRequest()).createResponse(").append(foo.name).append(".").append(method.id);
                           switch (argDecls.size())
                           {
                              case 0:
                                 break;
                              case 1:
                                 writer2.append(",(Object)").append(argDecls.get(0).getSimpleName());
                                 break;
                              default:
                                 writer2.append(",new Object[]{");
                                 for (int j = 0; j < argDecls.size(); j++)
                                 {
                                    if (j > 0)
                                    {
                                       writer2.append(",");
                                    }
                                    VariableElement argDecl = argDecls.get(j);
                                    writer2.append(argDecl.getSimpleName());
                                 }
                                 writer2.append("}");
                                 break;
                           }
                           writer2.append("); }\n");
                        }

                        // URL builder literal
                        writer2.append("public static URLBuilder ").append(method.getName()).append("URL").append("(");
                        for (int j = 0; j < argDecls.size(); j++)
                        {
                           if (j > 0)
                           {
                              writer2.append(',');
                           }
                           TypeMirror argumentType = argTypes.get(j);
                           VariableElement argDecl = argDecls.get(j);
                           writer2.append(argumentType.toString()).append(" ").append(argDecl.getSimpleName().toString());
                        }
                        writer2.append(") { return ((MimeContext)InternalApplicationContext.getCurrentRequest()).createURLBuilder(").append(foo.name).append(".").append(method.id);
                        switch (argDecls.size())
                        {
                           case 0:
                              break;
                           case 1:
                              writer2.append(",(Object)").append(argDecls.get(0).getSimpleName());
                              break;
                           default:
                              writer2.append(",new Object[]{");
                              for (int j = 0;j < argDecls.size();j++)
                              {
                                 if (j > 0)
                                 {
                                    writer2.append(",");
                                 }
                                 VariableElement argDecl = argDecls.get(j);
                                 writer2.append(argDecl.getSimpleName());
                              }
                              writer2.append("}");
                              break;
                        }
                        writer2.append("); }\n");
                     }

                     //
                     writer2.append("}\n");
                  }
                  finally
                  {
                     Tools.safeClose(writer2);
                  }

                  //
                  writer.append("public static final ").append(CONTROLLER_DESCRIPTOR).append(" controller").append(Integer.toString(controllerIndex));
                  writer.append(" = new ").append(CONTROLLER_DESCRIPTOR).append("(");
                  writer.append(controller.getClassName()).append(".class,");
                  writer.append("Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
                  int index = 0;
                  for (MethodMetaData method : controller.methods)
                  {
                     if (index > 0)
                     {
                        writer.append(",");
                     }
                     writer.append(method.id);
                     index++;
                  }
                  writer.append(")");
                  writer.append(");\n");

                  //
                  controllerIndex++;
               }

               //
               int count = 0;
               for (TemplateMetaData template : foo.templates)
               {
                  writer.append("public static final TemplateDescriptor template").append(Integer.toString(count)).append(" = ");
                  writer.append("new TemplateDescriptor(\"").append(template.path).append("\", ").append(template.className).append(".class);\n");
                  count++;
               }

               // Descriptor
               writer.append("public static final ApplicationDescriptor DESCRIPTOR = new ApplicationDescriptor(");
               writer.append("\"").append(foo.packageName).append("\",");
               writer.append("\"").append(foo.name).append("\",");
               writer.append(foo.defaultController).append(".class,");
               writer.append("\"").append(templatesPackageName).append("\",");

               // Controllers
               writer.append("Arrays.<").append(CONTROLLER_DESCRIPTOR).append(">asList(");
               int index = 0;
               for (ControllerMetaData bar : foo.controllers)
               {
                  if (index > 0)
                  {
                     writer.append(",");
                  }
                  writer.append("controller").append(Integer.toString(index));
                  index++;
               }
               writer.append("),");

               //
               writer.append("Arrays.<").append(TEMPLATE_DESCRIPTOR).append(">asList(");
               index = 0;
               for (TemplateMetaData templateMetaData : foo.templates)
               {
                  if (index > 0)
                  {
                     writer.append(',');
                  }
                  writer.append("template").append(Integer.toString(index));
                  index++;
               }
               writer.append(")");

               //
               writer.append(");\n");

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
         manifest.append(foo.name).append('=').append(foo.className).append("\n");
      }
   }

   @Override
   public void over()
   {
      try
      {
         FileObject fo = createResource(StandardLocation.CLASS_OUTPUT, "org.juzu", "config.properties");
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
}
