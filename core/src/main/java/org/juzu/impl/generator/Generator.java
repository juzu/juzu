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

package org.juzu.impl.generator;

import org.juzu.Path;
import org.juzu.Phase;
import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.inject.Export;
import org.juzu.impl.metamodel.ApplicationMetaModel;
import org.juzu.impl.metamodel.ControllerMetaModel;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.metamodel.MethodMetaModel;
import org.juzu.impl.metamodel.TemplateMetaModel;
import org.juzu.impl.metamodel.TemplateRefMetaModel;
import org.juzu.impl.processor.AnnotationHandler;
import org.juzu.impl.processor.ElementHandle;
import org.juzu.impl.processor.ErrorCode;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.TemplateCompilationContext;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Generator extends AnnotationHandler implements Serializable
{

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_METHOD = ControllerMethod.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_DESCRIPTOR = ControllerDescriptor.class.getSimpleName();

   /** . */
   private static final String CONTROLLER_PARAMETER = ControllerParameter.class.getSimpleName();

   /** . */
   private static final String PHASE = Phase.class.getSimpleName();

   /** . */
   private static final String TOOLS = Tools.class.getSimpleName();

   /** . */
   private static final String RESPONSE = Response.Render.class.getSimpleName();

   /** . */
   private ProcessingEnvironment env;

   /** . */
   private Map<String, TemplateProvider> providers;

   /** . */
   private Map<String, String> moduleConfig;

   /** . */
   private Map<ElementHandle.Package, Map<String, String>> applicationConfigs;

   /** . */
   private Map<ElementHandle.Package, TemplateRepository> templateRepositoryMap;

   /** . */
   private MetaModel metaModel;

   public Generator()
   {
      this.moduleConfig = new HashMap<String, String>();
      this.applicationConfigs = new HashMap<ElementHandle.Package, Map<String, String>>();
      this.metaModel = new MetaModel();
      this.templateRepositoryMap = new HashMap<ElementHandle.Package, TemplateRepository>();
   }

   @Override
   public void postActivate(ProcessingEnvironment env)
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
      this.env = env;
      this.providers = providers;

      //
      metaModel.postActivate(env);
   }

   @Override
   public void processControllerMethod(ExecutableElement methodElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException
   {
      metaModel.processControllerMethod(methodElt, annotationName, annotationValues);
   }

   @Override
   public void processDeclarationTemplate(VariableElement variableElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException
   {
      metaModel.processDeclarationTemplate(variableElt, annotationName, annotationValues);
   }

   @Override
   public void processApplication(PackageElement packageElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException
   {
      metaModel.processApplication(packageElt, annotationName, annotationValues);
   }

   @Override
   public void postProcess() throws CompilationException
   {
      metaModel.postProcess();

      //
      for (MetaModelEvent event : metaModel.popEvents())
      {
         processEvent(event);
      }

      //
      processTemplates();
   }

   private void processEvent(MetaModelEvent event) throws CompilationException
   {
      MetaModelObject obj = event.getObject();
      switch (event.getType())
      {
         case MetaModelEvent.AFTER_ADD:
            if (obj instanceof ApplicationMetaModel)
            {
               ApplicationMetaModel application = (ApplicationMetaModel)obj;
               moduleConfig.put(application.getFQN().getSimpleName(), application.getFQN().getFullName());
               applicationConfigs.put(application.getHandle(), new HashMap<String, String>());
               templateRepositoryMap.put(application.getHandle(), new TemplateRepository());
               emitApplication(application);
            }
            else if (obj instanceof ControllerMetaModel)
            {
               ControllerMetaModel controller = (ControllerMetaModel)obj;
               Map<String, String> foo = applicationConfigs.get(controller.getApplication().getHandle());
               foo.put(controller.getHandle().getFQN().getFullName() + "_", "controller");
               emitController(controller);
            }
            else if (obj instanceof TemplateMetaModel)
            {
               TemplateMetaModel template = (TemplateMetaModel)obj;

               //
               TemplateRepository repository = templateRepositoryMap.get(template.getApplication().getHandle());

               // Add the template to the repository
               repository.addTemplate(template);
            }
            else if (obj instanceof TemplateRefMetaModel)
            {
               // What should we do for now ?
            }
            else
            {
               throw new UnsupportedOperationException("Not yet supported add: " + obj);
            }
            break;
         case MetaModelEvent.BEFORE_REMOVE:
            throw new UnsupportedOperationException("Not yet supported remove: " + obj);
         case MetaModelEvent.UPDATED:
            if (obj instanceof ControllerMetaModel)
            {
               ControllerMetaModel controller = (ControllerMetaModel)obj;
               emitController(controller);
            }
            else
            {
               throw new UnsupportedOperationException("Not yet supported update: " + obj);
            }
            break;
      }
   }

   private void processTemplates()
   {
      for (Map.Entry<ElementHandle.Package, TemplateRepository> entry : templateRepositoryMap.entrySet())
      {
         TemplateRepository repo = entry.getValue();

         //
         final ApplicationMetaModel application = metaModel.getApplication(entry.getKey());

         //
         List<TemplateModel> addeds = repo.resolve(env);

         // Handle added templates
         for (TemplateModel added : addeds)
         {
            applicationConfigs.get(application.getHandle()).put(added.getFQN().getFullName(), "template");

            //
            TemplateMetaModel templateMeta = application.getTemplate(added.getOriginPath());

            //
            final Element[] elements = new Element[templateMeta.getRefs().size()];
            int index = 0;
            for (TemplateRefMetaModel ref : templateMeta.getRefs())
            {
               elements[index++] = ref.getHandle().get(env);
            }

            //
            TemplateProvider provider = providers.get(added.getExtension());
            Writer stubWriter = null;
            FQN stubFQN = added.getFQN();
            try
            {
               // Template stub
               JavaFileObject stubFile = env.getFiler().createSourceFile(stubFQN.getFullName() + "_", elements);
               stubWriter = stubFile.openWriter();
               stubWriter.append("package ").append(stubFQN.getPackageName()).append(";\n");
               stubWriter.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
               stubWriter.append("@Generated({\"").append(stubFQN.getFullName()).append("\"})\n");
               stubWriter.append("public class ").append(stubFQN.getSimpleName()).append("_ extends ").append(provider.getTemplateStubType().getName()).append(" {\n");
               stubWriter.append("}");

               //
               // log("Generated template stub " + stubFQN.getFullName() + "_" + " as " + stubFile.toUri());
            }
            catch (IOException e)
            {
               throw new CompilationException(e, entry.getKey().get(env), ErrorCode.CANNOT_WRITE_TEMPLATE_STUB_CLASS, added.getPath());
            }
            finally
            {
               Tools.safeClose(stubWriter);
            }

            //
            Writer classWriter = null;
            try
            {
               // Template qualified class
               FileObject classFile = env.getFiler().createSourceFile(stubFQN.getFullName(), elements);
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
                  classWriter.append("@Path(\"").append(added.getPath()).append("\")\n");
                  classWriter.append("public class ").append(stubFQN.getSimpleName()).append(" extends ").append(Template.class.getName()).append("\n");
                  classWriter.append("{\n");
                  classWriter.append("@Inject\n");
                  classWriter.append("public ").append(stubFQN.getSimpleName()).append("(").
                     append(ApplicationContext.class.getSimpleName()).append(" applicationContext").
                     append(")\n");
                  classWriter.append("{\n");
                  classWriter.append("super(applicationContext, \"").append(added.getPath()).append("\");\n");
                  classWriter.append("}\n");

                  //
                  if (added.getParameters() != null)
                  {
                     // Setters on template
                     for (String paramName : added.getParameters())
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
                     for (String paramName : added.getParameters())
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
                  // log("Generated template class " + stubFQN.getFullName() + " as " + classFile.toUri());
               }
               finally
               {
                  classWriter.close();
               }
            }
            catch (IOException e)
            {
               throw new CompilationException(e, entry.getKey().get(env), ErrorCode.CANNOT_WRITE_QUALIFIED_TEMPLATE_CLASS, added.getPath());
            }
            finally
            {
               Tools.safeClose(classWriter);
            }

            // Emit template file
            try
            {
               TemplateGenerator generator = provider.newGenerator();
               ASTNode.Template ast = added.getAST();
               ast.emit(new TemplateCompilationContext()
               {
                  @Override
                  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
                  {
                     MethodMetaModel method = application.resolve(typeName, methodName, parameterMap.keySet());

                     //
                     if (method == null)
                     {
                        throw new CompilationException(elements[0], ErrorCode.CONTROLLER_METHOD_NOT_FOUND, methodName, parameterMap);
                     }

                     //
                     List<String> args = new ArrayList<String>();
                     for (String parameterName : method.getParameterNames())
                     {
                        String value = parameterMap.get(parameterName);
                        args.add(value);
                     }
                     return new MethodInvocation(method.getController().getHandle().getFQN().getFullName() + "_", method.getName() + "URL", args);
                  }
               }, generator);

               //
               Collection<FileObject> generated = generator.generate(env.getFiler(), added.getFQN(), elements);
               if (generated.size() > 0)
               {
                  StringBuilder msg = new StringBuilder("Generated meta template ").append(added.getFQN().getFullName()).append(" as ");
                  int i = 0;
                  for (FileObject fo : generated)
                  {
                     msg.append(i++ == 0 ? "{" : ",").append(fo.toUri());
                  }
                  msg.append("}");
                  // log(msg);
               }
               else
               {
                  // log("Template " + template.getFQN().getFullName() + " generated no meta template");
               }
            }
            catch (IOException e)
            {
               throw new CompilationException(elements[0], ErrorCode.CANNOT_WRITE_TEMPLATE);
            }
         }
      }
   }

   @Override
   public void prePassivate() throws CompilationException
   {
      metaModel.prePassivate();

      //
      emitConfig();

      //
      this.providers = null;
      this.env = null;
   }

   private void emitConfig()
   {
      Properties config = new Properties();
      config.putAll(moduleConfig);
      Filer filer = env.getFiler();

      // Module config
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

      // Application configs
      for (Map.Entry<ElementHandle.Package, Map<String, String>> entry : applicationConfigs.entrySet())
      {
         config = new Properties();
         config.putAll(entry.getValue());

         //
         writer = null;
         try
         {
            FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, entry.getKey().getQN(), "config.properties");
            writer = fo.openWriter();
            config.store(writer, null);
         }
         catch (IOException e)
         {
            throw new CompilationException(e, entry.getKey().get(env), ErrorCode.CANNOT_WRITE_APPLICATION_CONFIG);
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
   }

   private void emitApplication(ApplicationMetaModel application) throws CompilationException
   {
      Filer filer = env.getFiler();

      //
      PackageElement elt = application.getHandle().get(env);
      FQN fqn = application.getFQN();

      //
      Writer writer = null;
      try
      {
         JavaFileObject applicationFile = filer.createSourceFile(fqn.getFullName(), elt);
         writer = applicationFile.openWriter();

         writer.append("package ").append(fqn.getPackageName()).append(";\n");

         // Imports
         writer.append("import ").append(Tools.getImport(ApplicationDescriptor.class)).append(";\n");

         // Open class
         writer.append("public class ").append(fqn.getSimpleName()).append(" extends ").append(APPLICATION_DESCRIPTOR).append(" {\n");

         // Singleton
         writer.append("public static final ").append(fqn.getSimpleName()).append(" DESCRIPTOR = new ").append(fqn.getSimpleName()).append("();\n");

         // Constructor
         writer.append("private ").append(fqn.getSimpleName()).append("() {\n");
         writer.append("super(");
         writer.append(application.getDefaultController() != null ? (application.getDefaultController() + ".class") : "null");
         writer.append(",");
         writer.append("\"").append(application.getTemplatesQN()).append("\"");
         writer.append(");\n");
         writer.append("}\n");

         // Close class
         writer.append("}\n");

         //
//         log("Generated application " + fqn.getFullName() + " as " + applicationFile.toUri());
      }
      catch (IOException e)
      {
         throw new CompilationException(e, elt, ErrorCode.CANNOT_WRITE_APPLICATION_CLASS);
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }

   private void emitController(ControllerMetaModel controller) throws CompilationException
   {
      Filer filer = env.getFiler();

      FQN fqn = controller.getHandle().getFQN();

      Element origin = controller.getHandle().get(env);
      
      // Generate controller literal
      Writer writer = null;
      try
      {
         JavaFileObject applicationFile = filer.createSourceFile(fqn.getFullName() + "_", origin);
         writer = applicationFile.openWriter();

         //
         writer.append("package ").append(fqn.getPackageName()).append(";\n");

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
         writer.append("public class ").append(fqn.getSimpleName()).append("_ extends ").append(CONTROLLER_DESCRIPTOR).append(" {\n");

         //
         writer.append("private ").append(fqn.getSimpleName()).append("_() {\n");
         writer.append("super(").append(fqn.getSimpleName()).append(".class, Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
         List<MethodMetaModel> methods = controller.getMethods();
         for (int j = 0;j < methods.size();j++)
         {
            MethodMetaModel method = methods.get(j);
            if (j > 0)
            {
               writer.append(',');
            }
            writer.append(method.getId());
         }
         writer.append("));\n");
         writer.append("}\n");

         //
         for (MethodMetaModel method : methods)
         {
            // Method constant
            writer.append("private static final ").append(CONTROLLER_METHOD).append(" ").append(method.getId()).append(" = ");
            writer.append("new ").append(CONTROLLER_METHOD).append("(");
            writer.append("\"").append(method.getId()).append("\",");
            writer.append(PHASE).append(".").append(method.getPhase().name()).append(",");
            writer.append(fqn.getFullName()).append(".class").append(",");
            writer.append(TOOLS).append(".safeGetMethod(").append(fqn.getFullName()).append(".class,\"").append(method.getName()).append("\"");
            for (String parameterType : method.getParameterTypes())
            {
               writer.append(",").append(parameterType).append(".class");
            }
            writer.append(")");
            writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
            for (Iterator<String> j = method.getParameterNames().iterator();j.hasNext(); )
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
            if (method.getPhase() == Phase.RENDER)
            {
               writer.append("public static ").append(RESPONSE).append(" ").append(method.getName()).append("(");
               for (int j = 0; j < method.getParameterTypes().size(); j++)
               {
                  if (j > 0)
                  {
                     writer.append(',');
                  }
                  writer.append(method.getParameterTypes().get(j)).append(" ").append(method.getParameterNames().get(j));
               }
               writer.append(") { return ((ActionContext)InternalApplicationContext.getCurrentRequest()).createResponse(").append(method.getId());
               switch (method.getParameterTypes().size())
               {
                  case 0:
                     break;
                  case 1:
                     writer.append(",(Object)").append(method.getParameterNames().get(0));
                     break;
                  default:
                     writer.append(",new Object[]{");
                     for (int j = 0; j < method.getParameterNames().size();j++)
                     {
                        if (j > 0)
                        {
                           writer.append(",");
                        }
                        writer.append(method.getParameterNames().get(j));
                     }
                     writer.append("}");
                     break;
               }
               writer.append("); }\n");
            }

            // URL builder literal
            writer.append("public static URLBuilder ").append(method.getName()).append("URL").append("(");
            for (int j = 0; j < method.getParameterTypes().size(); j++)
            {
               if (j > 0)
               {
                  writer.append(',');
               }
               writer.append(method.getParameterTypes().get(j)).append(" ").append(method.getParameterNames().get(j));
            }
            writer.append(") { return ((MimeContext)InternalApplicationContext.getCurrentRequest()).createURLBuilder(").append(method.getId());
            switch (method.getParameterNames().size())
            {
               case 0:
                  break;
               case 1:
                  writer.append(",(Object)").append(method.getParameterNames().get(0));
                  break;
               default:
                  writer.append(",new Object[]{");
                  for (int j = 0;j < method.getParameterNames().size();j++)
                  {
                     if (j > 0)
                     {
                        writer.append(",");
                     }
                     writer.append(method.getParameterNames().get(j));
                  }
                  writer.append("}");
                  break;
            }
            writer.append("); }\n");
         }

         // Singleton instance (declared after the method constants)
         writer.append("public static final ").append(fqn.getSimpleName()).append("_ INSTANCE = new ").append(fqn.getSimpleName()).append("_();\n");

         // Close class
         writer.append("}\n");

         //
         // log("Generated controller companion " + fqn.getFullName() + "_" + " as " + applicationFile.toUri());
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
