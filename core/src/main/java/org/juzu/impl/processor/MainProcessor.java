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

import org.juzu.Path;
import org.juzu.Phase;
import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.inject.Export;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.utils.Tools;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.request.ActionContext;
import org.juzu.request.ApplicationContext;
import org.juzu.request.MimeContext;
import org.juzu.template.Template;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
@javax.annotation.processing.SupportedOptions("foobar")
@javax.annotation.processing.SupportedAnnotationTypes({

   "org.juzu.View","org.juzu.Action","org.juzu.Resource",

   "org.juzu.Application",

   "org.juzu.Path"

})
public class MainProcessor extends BaseProcessor
{

   /** . */
   private static final String RESPONSE = Response.Render.class.getSimpleName();

   /** . */
   private static final String PHASE = Phase.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_METHOD = ControllerMethod.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_DESCRIPTOR = ControllerDescriptor.class.getSimpleName();

   /** . */
   private static final String TOOLS = Tools.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_PARAMETER = ControllerParameter.class.getSimpleName();

   /** . */
   private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   private static final ThreadLocal<ProcessingEnvironment> env = new ThreadLocal<ProcessingEnvironment>();

   /** . */
   private Model model;

   /** . */
   Filer filer;

   /** . */
   private Map<String, TemplateProvider> providers;

   static Element get(ElementHandle handle)
   {
      return handle.get(env.get());
   }

   @Override
   protected void doInit(ProcessingEnvironment processingEnv)
   {
      // Discover the template providers
      ServiceLoader<TemplateProvider> loader = ServiceLoader.load(TemplateProvider.class, TemplateProvider.class.getClassLoader());
      Map<String, TemplateProvider> providers = new HashMap<String, TemplateProvider>();
      for (TemplateProvider provider : loader)
      {
         // Get extension
         String pkgName = provider.getClass().getPackage().getName();

         //
         Matcher matcher = PROVIDER_PKG_PATTERN.matcher(pkgName);
         if (matcher.matches())
         {
            String extension = matcher.group(1);
            providers.put(extension, provider);
         }
      }

      //
      this.providers = providers;
      this.filer = processingEnv.getFiler();

      //
      String options = processingEnv.getOptions().toString();
      log("Using processing nev " + processingEnv.getClass().getName());
   }

   @Override
   protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      try
      {
         env.set(processingEnv);

         //
         if (!roundEnv.errorRaised())
         {
            if (roundEnv.processingOver())
            {
               // Emit templates
               emitTemplates();

               // Emit config
               emitConfig();

               // Passivate model
               ObjectOutputStream out = null;
               try
               {
                  FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "model.ser");
                  out = new ObjectOutputStream(file.openOutputStream());
                  out.writeObject(model);
                  model = null;
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
               finally
               {
                  Tools.safeClose(out);
               }
            }
            else
            {
               if (model == null)
               {
                  InputStream in = null;
                  try
                  {
                     FileObject file = filer.getResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "model.ser");
                     in = file.openInputStream();
                     ObjectInputStream ois = new ObjectInputStream(in);
                     model = (Model)ois.readObject();
                  }
                  catch (Exception e)
                  {
                     model = new Model();
                  }
                  finally
                  {
                     Tools.safeClose(in);
                  }
               }

               //
               for (TypeElement annotationElt : annotations)
               {
                  for (Element annotatedElt : roundEnv.getElementsAnnotatedWith(annotationElt))
                  {
                     if (annotatedElt.getAnnotation(Generated.class) == null)
                     {
                        for (AnnotationMirror annotationMirror : annotatedElt.getAnnotationMirrors())
                        {
                           if (annotationMirror.getAnnotationType().asElement().equals(annotationElt))
                           {
                              String annotationName = annotationElt.getSimpleName().toString();
                              Map<String, Object> annotationValues = new HashMap<String, Object>();
                              for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
                              {
                                 String m = entry.getKey().getSimpleName().toString();
                                 Object value = entry.getValue().getValue();
                                 annotationValues.put(m, value);
                              }
                              String annotationFQN = annotationElt.getQualifiedName().toString();
                              if (annotationFQN.equals("org.juzu.View") || annotationFQN.equals("org.juzu.Action") || annotationFQN.equals("org.juzu.Resource"))
                              {
                                 processController(annotationName, annotationValues, annotatedElt);
                              }
                              else if (annotationFQN.equals("org.juzu.Path"))
                              {
                                 processTemplate(annotationName, annotationValues, annotatedElt);
                              }
                              else if (annotationFQN.equals("org.juzu.Application"))
                              {
                                 processApplication(annotationName, annotationValues, annotatedElt);
                              }
                              break;
                           }
                        }
                     }
                  }
               }

               //
               resolveControllers();

               //
               resolveTemplates();
            }
         }
      }
      finally
      {
         env.set(null);
      }
   }

   private void emitConfig()
   {
      // Config properties
      Properties config = new Properties();
      for (ApplicationModel application : model.applications.values())
      {
         config.put(application.fqn.getSimpleName(), application.fqn.getFullName());
      }

      //
      Writer writer = null;
      try
      {
         //
         FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "org.juzu", "config.properties");
         writer = fo.openWriter();
         config.store(writer, null);
      }
      catch (IOException e)
      {
         throw new CompilationException(e, ErrorCode.CANNOT_WRITE_CONFIG);
      }
      finally
      {
         Tools.safeClose(writer);
      }

      // Config xml
      for (ApplicationModel application : model.applications.values())
      {
         config = new Properties();
         for (ControllerModel controller : application.controllers.values())
         {
            config.put(controller.fqn.getFullName() + "_", "controller");
         }
         for (TemplateModel template : application.templates.values())
         {
            config.put(template.getFQN().getFullName(), "template");
         }

         //
         writer = null;
         try
         {
            FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, application.fqn.getPackageName().getValue(), "config.properties");
            writer = fo.openWriter();
            config.store(writer, null);
         }
         catch (IOException e)
         {
            throw new CompilationException(e, application.origin.get(processingEnv), ErrorCode.CANNOT_WRITE_APPLICATION_CONFIG);
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
   }

   private void emitTemplates()
   {
      for (final ApplicationModel application : model.applications.values())
      {
         for (final TemplateModel template : application.templates.values())
         {
            TemplateProvider provider = providers.get(template.getFoo().getExtension());

            // Emit template file
            try
            {
               TemplateGenerator generator = provider.newGenerator();
               ASTNode.Template ast = template.getAST();
               ast.emit(new TemplateCompilationContext()
               {
                  @Override
                  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
                  {
                     MethodModel method = application.resolve(typeName, methodName, parameterMap.keySet());

                     //
                     if (method == null)
                     {
                        throw new CompilationException(template.getFoo().getOrigin().get(processingEnv), ErrorCode.CONTROLLER_METHOD_NOT_FOUND, methodName, parameterMap);
                     }

                     //
                     List<String> args = new ArrayList<String>();
                     for (String parameterName : method.parameterNames)
                     {
                        String value = parameterMap.get(parameterName);
                        args.add(value);
                     }
                     return new MethodInvocation(method.controller.fqn + "_", method.name + "URL", args);
                  }
               }, generator);

               //
               Collection<FileObject> generated = generator.generate(filer, template.getFQN());
               if (generated.size() > 0)
               {
                  StringBuilder msg = new StringBuilder("Generated meta template ").append(template.getFQN().getFullName()).append(" as ");
                  int i = 0;
                  for (FileObject fo : generated)
                  {
                     msg.append(i++ == 0 ? "{" : ",").append(fo.toUri());
                  }
                  msg.append("}");
                  log(msg);
               }
               else
               {
                  log("Template " + template.getFQN().getFullName() + " generated no meta template");
               }
            }
            catch (IOException e)
            {
               throw new CompilationException(template.getFoo().getOrigin().get(processingEnv), ErrorCode.CANNOT_WRITE_TEMPLATE);
            }
         }
      }
   }

   private void resolveControllers()
   {
      log("Begin controller resolution");

      //
      for (Iterator<Map.Entry<String, ControllerModel>> i = model.controllers.entrySet().iterator();i.hasNext();)
      {
         Map.Entry<String, ControllerModel> controllerEntry = i.next();
         for (Map.Entry<String, ApplicationModel> applicationEntry : model.applications.entrySet())
         {
            if (controllerEntry.getKey().startsWith(applicationEntry.getKey() + "."))
            {
               i.remove();
               ApplicationModel application = applicationEntry.getValue();
               ControllerModel controller = controllerEntry.getValue();

               // Validate duplicate controllers
               // todo

               //
               application.controllers.put(controllerEntry.getKey(), controller);

               //
               Element origin = controller.origin.get(processingEnv);

               // Generate controller literal
               Writer writer = null;
               try
               {
                  JavaFileObject applicationFile = filer.createSourceFile(controller.fqn.getFullName() + "_", origin);
                  writer = applicationFile.openWriter();

                  //
                  writer.append("package ").append(controller.fqn.getPackageName()).append(";\n");

                  // Imports
                  writer.append("import ").append(Tools.getImport(ControllerMethod.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(ControllerParameter.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(Tools.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(Arrays.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(Phase.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(URLBuilder.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(InternalApplicationContext.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(MimeContext.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(ActionContext.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(Response.Render.class)).append(";\n");
                  writer.append("import ").append(Tools.getImport(ControllerDescriptor.class)).append(";\n");

                  // Open class
                  writer.append("public class ").append(controller.fqn.getSimpleName()).append("_ extends ").append(CONTROLLER_DESCRIPTOR).append(" {\n");

                  //
                  writer.append("private ").append(controller.fqn.getSimpleName()).append("_() {\n");
                  writer.append("super(").append(controller.fqn.getSimpleName()).append(".class, Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
                  for (int j = 0;j < controller.methods.size();j++)
                  {
                     MethodModel method = controller.methods.get(j);
                     if (j > 0)
                     {
                        writer.append(',');
                     }
                     writer.append(method.id);
                  }
                  writer.append("));\n");
                  writer.append("}\n");

                  //
                  for (MethodModel method : controller.methods)
                  {
                     // Method constant
                     writer.append("private static final ").append(CONTROLLER_METHOD).append(" ").append(method.id).append(" = ");
                     writer.append("new ").append(CONTROLLER_METHOD).append("(");
                     writer.append("\"").append(method.id).append("\",");
                     writer.append(PHASE).append(".").append(method.phase.name()).append(",");
                     writer.append(controller.fqn.getFullName()).append(".class").append(",");
                     writer.append(TOOLS).append(".safeGetMethod(").append(controller.fqn.getFullName()).append(".class,\"").append(method.name).append("\"");
                     for (String parameterType : method.parameterTypes)
                     {
                        writer.append(",").append(parameterType).append(".class");
                     }
                     writer.append(")");
                     writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
                     for (Iterator<String> j = method.parameterNames.iterator();j.hasNext(); )
                     {
                        String parameterName = j.next();
                        writer.append("new ").append(CONTROLLER_PARAMETER).append("(\"").
                           append(parameterName).append("\")");
                        if (j.hasNext())
                        {
                           writer.append(",");
                        }
                     }
                     writer.append(")");
                     writer.append(");\n");

                     // Render builder literal
                     if (method.phase == Phase.RENDER)
                     {
                        writer.append("public static ").append(RESPONSE).append(" ").append(method.name).append("(");
                        for (int j = 0; j < method.parameterTypes.size(); j++)
                        {
                           if (j > 0)
                           {
                              writer.append(',');
                           }
                           writer.append(method.parameterTypes.get(j)).append(" ").append(method.parameterNames.get(j));
                        }
                        writer.append(") { return ((ActionContext)InternalApplicationContext.getCurrentRequest()).createResponse(").append(method.id);
                        switch (method.parameterTypes.size())
                        {
                           case 0:
                              break;
                           case 1:
                              writer.append(",(Object)").append(method.parameterNames.get(0));
                              break;
                           default:
                              writer.append(",new Object[]{");
                              for (int j = 0; j < method.parameterNames.size();j++)
                              {
                                 if (j > 0)
                                 {
                                    writer.append(",");
                                 }
                                 writer.append(method.parameterNames.get(j));
                              }
                              writer.append("}");
                              break;
                        }
                        writer.append("); }\n");
                     }

                     // URL builder literal
                     writer.append("public static URLBuilder ").append(method.name).append("URL").append("(");
                     for (int j = 0; j < method.parameterTypes.size(); j++)
                     {
                        if (j > 0)
                        {
                           writer.append(',');
                        }
                        writer.append(method.parameterTypes.get(j)).append(" ").append(method.parameterNames.get(j));
                     }
                     writer.append(") { return ((MimeContext)InternalApplicationContext.getCurrentRequest()).createURLBuilder(").append(method.id);
                     switch (method.parameterNames.size())
                     {
                        case 0:
                           break;
                        case 1:
                           writer.append(",(Object)").append(method.parameterNames.get(0));
                           break;
                        default:
                           writer.append(",new Object[]{");
                           for (int j = 0;j < method.parameterNames.size();j++)
                           {
                              if (j > 0)
                              {
                                 writer.append(",");
                              }
                              writer.append(method.parameterNames.get(j));
                           }
                           writer.append("}");
                           break;
                     }
                     writer.append("); }\n");
                  }

                  // Singleton instance (declared after the method constants)
                  writer.append("public static final ").append(controller.fqn.getSimpleName()).append("_ INSTANCE = new ").append(controller.fqn.getSimpleName()).append("_();\n");

                  // Close class
                  writer.append("}\n");

                  //
                  log("Generated controller companion " + controller.fqn.getFullName() + "_" + " as " + applicationFile.toUri());
               }
               catch (IOException e)
               {
                  throw new CompilationException(e, origin, ErrorCode.CANNOT_WRITE_CONTROLLER_CLASS);
               }
               finally
               {
                  Tools.safeClose(writer);
               }
            }
         }
      }

      //
      log("End controller resolution");
   }

   private void resolveTemplates()
   {
      log("Begin template resolution");

      //
      next:
      for (Iterator<Foo> i = model.templates.iterator();i.hasNext();)
      {
         Foo foo = i.next();
         for (Map.Entry<String, ApplicationModel> applicationEntry : model.applications.entrySet())
         {
            if (
               foo.getOriginPackageFQN().equals(applicationEntry.getKey()) ||
               foo.getOriginPackageFQN().startsWith(applicationEntry.getKey() + "."))
            {
               ApplicationModel application = applicationEntry.getValue();
               String path = foo.getPath();

               //
               i.remove();
               log("Resolving template " + path + " to application " + application.fqn);

               // Process template
               TemplateCompiler compiler;
               try
               {
                  compiler = new TemplateCompiler(application, foo, this);

                  //
                  compiler.resolveTemplate(path);
               }
               catch (IOException e)
               {
                  throw new UnsupportedOperationException("handle me gracefully: could not process template " + path);
               }

               for (TemplateModel template : compiler.getAdded())
               {
                  TemplateProvider provider = providers.get(template.getFoo().getExtension());
                  Writer stubWriter = null;
                  FQN stubFQN = template.getFQN();
                  try
                  {
                     // Template stub
                     JavaFileObject stubFile = filer.createSourceFile(stubFQN.getFullName() + "_");
                     stubWriter = stubFile.openWriter();
                     stubWriter.append("package ").append(stubFQN.getPackageName()).append(";\n");
                     stubWriter.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
                     stubWriter.append("@Generated({\"").append(stubFQN.getFullName()).append("\"})\n");
                     stubWriter.append("public class ").append(stubFQN.getSimpleName()).append("_ extends ").append(provider.getTemplateStubType().getName()).append(" {\n");
                     stubWriter.append("}");

                     //
                     log("Generated template stub " + stubFQN.getFullName() + "_" + " as " + stubFile.toUri());
                  }
                  catch (IOException e)
                  {
                     throw new CompilationException(e, template.getFoo().getOrigin().get(processingEnv), ErrorCode.CANNOT_WRITE_TEMPLATE_STUB_CLASS, template.getFoo().getPath());
                  }
                  finally
                  {
                     Tools.safeClose(stubWriter);
                  }
                  Writer classWriter = null;
                  try
                  {
                     // Template qualified class
                     FileObject classFile = filer.createSourceFile(stubFQN.getFullName());
                     classWriter = classFile.openWriter();
                     try
                     {
                        classWriter.append("package ").append(stubFQN.getPackageName()).append(";\n");
                        classWriter.append("import ").append(Tools.getImport(Path.class)).append(";\n");
                        classWriter.append("import ").append(Tools.getImport(Export.class)).append(";\n");
                        classWriter.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
                        classWriter.append("import javax.inject.Inject;\n");
                        classWriter.append("import ").append(Tools.getImport(ApplicationContext.class)).append(";\n");
                        classWriter.append("@Generated({})\n");
                        classWriter.append("@Export\n");
                        classWriter.append("@Path(\"").append(template.getFoo().getPath()).append("\")\n");
                        classWriter.append("public class ").append(stubFQN.getSimpleName()).append(" extends ").append(Template.class.getName()).append("\n");
                        classWriter.append("{\n");
                        classWriter.append("@Inject\n");
                        classWriter.append("public ").append(stubFQN.getSimpleName()).append("(").
                           append(ApplicationContext.class.getSimpleName()).append(" applicationContext").
                           append(")\n");
                        classWriter.append("{\n");
                        classWriter.append("super(applicationContext, \"").append(path).append("\");\n");
                        classWriter.append("}\n");

                        //
                        if (template.getParameters() != null)
                        {
                           // Setters on template
                           for (String paramName : template.getParameters())
                           {
                              classWriter.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
                              classWriter.append("Builder builder = new Builder();");
                              classWriter.append("builder.set(\"").append(paramName).append("\",").append(paramName).append(");\n");
                              classWriter.append("return builder;\n");
                              classWriter.append(("}\n"));
                           }

                           // Setters on builders
                           classWriter.append("public class Builder extends ").append(Tools.getImport(Template.Builder.class)).append("\n");
                           classWriter.append("{\n");
                           for (String paramName : template.getParameters())
                           {
                              classWriter.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
                              classWriter.append("set(\"").append(paramName).append("\",").append(paramName).append(");\n");
                              classWriter.append("return this;\n");
                              classWriter.append(("}\n"));
                           }
                           classWriter.append("}\n");
                        }

                        // Close class
                        classWriter.append("}\n");

                        //
                        log("Generated template class " + stubFQN.getFullName() + " as " + classFile.toUri());
                     }
                     finally
                     {
                        classWriter.close();
                     }
                  }
                  catch (IOException e)
                  {
                     throw new CompilationException(e, template.getFoo().getOrigin().get(processingEnv), ErrorCode.CANNOT_WRITE_QUALIFIED_TEMPLATE_CLASS, template.getFoo().getPath());
                  }
                  finally
                  {
                     Tools.safeClose(classWriter);
                  }
               }

               //
               continue next;
            }
         }
      }

      //
      log("End template resolution");
   }

   private void processController(
      String annotationName,
      Map<String, Object> annotationValues,
      Element annotatedElt) throws CompilationException
   {
      ExecutableElement methodElt = (ExecutableElement)annotatedElt;
      TypeElement controllerElt = (TypeElement)methodElt.getEnclosingElement();
      FQN controllerFQN = new FQN(controllerElt.getQualifiedName().toString());
      ControllerModel controller = model.controllers.get(controllerFQN.getFullName());
      if (controller == null)
      {
         ElementHandle origin = ElementHandle.Class.create(controllerElt);
         model.controllers.put(controllerFQN.getFullName(), controller = new ControllerModel(origin, controllerFQN));
      }
      String id = (String)annotationValues.get("id");
      if (id == null)
      {
         // Temporary
         id = "method_" + Math.abs(new Random().nextInt());
      }
      for (Phase phase : Phase.values())
      {
         if (phase.annotation.getSimpleName().equals(annotationName))
         {
            ArrayList<String> parameterTypes = new ArrayList<String>();
            for (TypeMirror parameterType : ((ExecutableType)methodElt.asType()).getParameterTypes())
            {
               TypeMirror erasedParameterType = processingEnv.getTypeUtils().erasure(parameterType);
               parameterTypes.add(erasedParameterType.toString());
            }
            ArrayList<String> parameterNames = new ArrayList<String>();
            for (VariableElement variableElt : methodElt.getParameters())
            {
               parameterNames.add(variableElt.getSimpleName().toString());
            }

            // Validate duplicate id within the same controller
            for (MethodModel existing : controller.methods)
            {
               if (existing.id.equals(id))
               {
                  throw new CompilationException(annotatedElt, ErrorCode.DUPLICATE_CONTROLLER_ID, id);
               }
            }

            //
            MethodModel method = new MethodModel(
               controller,
               id,
               phase,
               methodElt.getSimpleName().toString(),
               parameterTypes,
               parameterNames);
            controller.methods.add(method);
            break;
         }
      }
   }

   private void processTemplate(
      String annotationName,
      Map<String, Object> annotationValues,
      Element annotatedElt) throws CompilationException
   {
      String path = (String)annotationValues.get("value");
      PackageElement packageElt = processingEnv.getElementUtils().getPackageOf(annotatedElt);
      Foo foo = new Foo(
         annotatedElt,
         packageElt.getQualifiedName().toString(),
         path
      );
      model.templates.add(foo);
      log("Found template " + path);
   }

   private void processApplication(
      String annotationName,
      Map<String, Object> annotationValues,
      Element annotatedElt) throws CompilationException
   {
      //
      PackageElement packageElt = (PackageElement)annotatedElt;
      TypeMirror defaultControllerElt = (TypeMirror)annotationValues.get("defaultController");
      String defaultController = defaultControllerElt != null ? defaultControllerElt.toString() : null;
      String name = (String)annotationValues.get("name");
      if (name == null)
      {
         String s = packageElt.getSimpleName().toString();
         name = Character.toUpperCase(s.charAt(0)) + s.substring(1) + "Application";
      }
      String packageName = packageElt.getQualifiedName().toString();
      String templatesFQN = packageName.isEmpty() ? "templates" : (packageName + ".templates");
      FQN fqn = new FQN(packageName, name);
      ApplicationModel application = new ApplicationModel(
         ElementHandle.Package.create(packageElt),
         fqn,
         defaultController,
         templatesFQN);
      model.applications.put(packageName, application);
      log("Found application" + name + " as " + fqn);

      //
      Writer writer = null;
      try
      {
         JavaFileObject applicationFile = filer.createSourceFile(application.fqn.getFullName(), annotatedElt);
         writer = applicationFile.openWriter();

         writer.append("package ").append(application.fqn.getPackageName()).append(";\n");

         // Imports
         writer.append("import ").append(Tools.getImport(ApplicationDescriptor.class)).append(";\n");

         // Open class
         writer.append("public class ").append(application.fqn.getSimpleName()).append(" extends ").append(APPLICATION_DESCRIPTOR).append(" {\n");

         // Singleton
         writer.append("public static final ").append(application.fqn.getSimpleName()).append(" DESCRIPTOR = new ").append(application.fqn.getSimpleName()).append("();\n");

         // Constructor
         writer.append("private ").append(application.fqn.getSimpleName()).append("() {\n");
         writer.append("super(");
         writer.append(application.defaultController != null ? (application.defaultController + ".class") : "null");
         writer.append(",");
         writer.append("\"").append(application.templatesFQN).append("\"");
         writer.append(");\n");
         writer.append("}\n");

         // Close class
         writer.append("}\n");

         //
         log("Generated application " + application.fqn.getFullName() + " as " + applicationFile.toUri());
      }
      catch (IOException e)
      {
         throw new CompilationException(e, annotatedElt, ErrorCode.CANNOT_WRITE_APPLICATION_CLASS);
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }
}
