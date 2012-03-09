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

package org.juzu.impl.template.metamodel;

import org.juzu.Path;
import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.controller.metamodel.MethodMetaModel;
import org.juzu.impl.inject.Export;
import org.juzu.impl.metamodel.MetaModelErrorCode;
import org.juzu.impl.metamodel.ProcessingContext;
import org.juzu.impl.spi.template.TemplateEmitter;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.compiler.Template;
import org.juzu.impl.template.compiler.EmitContext;
import org.juzu.impl.template.compiler.EmitPhase;
import org.juzu.impl.template.metadata.TemplateDescriptor;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.utils.Tools;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The template repository.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateResolver implements Serializable
{

   /** . */
   private static final Logger log = BaseProcessor.getLogger(TemplateResolver.class);

   /** . */
   private final ApplicationMetaModel application;

   /** . */
   private Map<String, Template> templates;

   /** . */
   private Map<String, FileObject> resourceCache;
   
   /** . */
   private Map<FQN, FileObject> stubCache;

   /** . */
   private Map<FQN, FileObject> classCache;

   public TemplateResolver(ApplicationMetaModel application)
   {
      if (application == null)
      {
         throw new NullPointerException();
      }

      //
      this.application = application;
      this.templates = new HashMap<String, Template>();
      this.resourceCache = new HashMap<String, FileObject>();
      this.stubCache = new HashMap<FQN, FileObject>();
      this.classCache = new HashMap<FQN, FileObject>();
   }

   public Collection<Template> getTemplates()
   {
      return templates.values();
   }
   
   public void removeTemplate(String path)
   {
      // Shall we do something else ?
      templates.remove(path);
   }

   public void prePassivate()
   {
      log.log("Evicting cache " + resourceCache.keySet());
      resourceCache.clear();
      stubCache.clear();
      classCache.clear();
   }

   public void process(TemplatePlugin plugin, ProcessingContext context) throws CompilationException
   {
      // Evict templates that are out of date
      log.log("Synchronizing existing templates " + templates.keySet());
      for (Iterator<Template> i = templates.values().iterator();i.hasNext();)
      {
         Template template = i.next();
         Content content = context.resolveResource(application.getHandle(), template.getFQN(), template.getExtension());
         if (content == null)
         {
            // That will generate a template not found error
            i.remove();
            log.log("Detected template removal " + template.getFQN());
         }
         else if (content.getLastModified() > template.getLastModified())
         {
            // That will force the regeneration of the template
            i.remove();
            log.log("Detected stale template " + template.getFQN());
         }
         else
         {
            log.log("Template " + template.getFQN() + " is valid");
         }
      }

      // Build missing templates
      log.log("Building missing templates");
      Map<String, Template> copy = new HashMap<String, Template>(templates);
      for (TemplateMetaModel templateMeta : application.getTemplates())
      {
         Template template = copy.get(templateMeta.getPath());
         if (template == null)
         {
            log.log("Compiling template " + templateMeta.getPath());
            ModelTemplateProcessContext compiler = new ModelTemplateProcessContext(templateMeta, new HashMap<String, Template>(copy), context);
            Collection<Template> resolved = compiler.resolve(templateMeta);
            for (Template added : resolved)
            {
               copy.put(added.getPath(), added);
            }
         }
      }
      templates = copy;

      // Generate missing files from template
      for (Template template : templates.values())
      {
         //
         String originPath = template.getOriginPath();
         TemplateMetaModel templateMeta = application.getTemplates().get(originPath);

         //
         // We compute the class elements from the field elements (as eclipse will make the relationship)
         Set<FQN> types = new LinkedHashSet<FQN>();
         for (TemplateRefMetaModel ref : templateMeta.getRefs())
         {
            ElementHandle.Field handle = ref.getHandle();
            types.add(handle.getFQN());
         }
         final Element[] elements = new Element[types.size()];
         int index = 0;
         for (FQN type : types)
         {
            elements[index++] = context.getTypeElement(type.getFullName());
         }

         // Resolve the stub
         resolveStub(template, plugin, context, elements);

         // Resolve the qualified class
         resolvedQualified(template, context, elements);

         //
         resolveScript(template, plugin, context, elements);
      }
   }

   private void resolveScript(final Template template, final TemplatePlugin plugin, final ProcessingContext context, final Element[] elements)
   {
      context.executeWithin(elements[0], new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TemplateProvider provider = plugin.providers.get(template.getExtension());

            // If it's the cache we do nothing
            String key = template.getFQN().getFullName() + ".groovy";
            if (!resourceCache.containsKey(key))
            {
               //
               Writer writer = null;
               try
               {
                  TemplateEmitter generator = provider.createEmitter();
                  ASTNode.Template ast = template.getAST();
                  EmitPhase tcc = new EmitPhase(new EmitContext()
                  {
                     @Override
                     public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) throws CompilationException
                     {
                        MethodMetaModel method = application.getControllers().resolve(typeName, methodName, parameterMap.keySet());

                        //
                        if (method == null)
                        {
                           throw new CompilationException(MetaModelErrorCode.CONTROLLER_METHOD_NOT_RESOLVED, methodName + "(" + parameterMap + ")");
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
                  });

                  //
                  tcc.emit(generator, ast);

                  //
                  FileObject scriptFile = context.createResource(StandardLocation.CLASS_OUTPUT, template.getFQN().getPackageName(), template.getFQN().getSimpleName() + "." + provider.getTargetExtension(), elements);
                  writer = scriptFile.openWriter();
                  writer.write(generator.toString());

                  // Put it in cache
                  resourceCache.put(key, scriptFile);

                  //
                  log.log("Generated template script " + template.getFQN().getFullName() + " as " + scriptFile.toUri() +
                     " with originating elements " + Arrays.asList(elements));
               }
               catch (IOException e)
               {
                  throw new CompilationException(e, MetaModelErrorCode.CANNOT_WRITE_TEMPLATE_SCRIPT, template.getPath());
               }
               finally
               {
                  Tools.safeClose(writer);
               }
            }
            else
            {
               log.log("Template " + key + " was found in cache");
            }

            //
            return null;
         }
      });
   }

   private void resolvedQualified(Template template, ProcessingContext context, Element[] elements)
   {
      if (classCache.containsKey(template.getFQN()))
      {
         log.log("Template class " + template.getFQN() + " was found in cache");
         return;
      }

      //
      Writer writer = null;
      try
      {
         // Template qualified class
         FileObject classFile = context.createSourceFile(template.getFQN().getFullName(), elements);
         writer = classFile.openWriter();
         writer.append("package ").append(template.getFQN().getPackageName()).append(";\n");
         writer.append("import ").append(Tools.getImport(Path.class)).append(";\n");
         writer.append("import ").append(Tools.getImport(Export.class)).append(";\n");
         writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
         writer.append("import ").append(Tools.getImport(TemplateDescriptor.class)).append(";\n");
         writer.append("import javax.inject.Inject;\n");
         writer.append("import ").append(Tools.getImport(ApplicationContext.class)).append(";\n");
         writer.append("@Generated({})\n");
         writer.append("@Export\n");
         writer.append("@Path(\"").append(template.getPath()).append("\")\n");
         writer.append("public class ").append(template.getFQN().getSimpleName()).append(" extends ").append(org.juzu.template.Template.class.getName()).append("\n");
         writer.append("{\n");
         writer.append("@Inject\n");
         writer.append("public ").append(template.getFQN().getSimpleName()).append("(").
            append(ApplicationContext.class.getSimpleName()).append(" applicationContext").
            append(")\n");
         writer.append("{\n");
         writer.append("super(applicationContext, \"").append(template.getPath()).append("\");\n");
         writer.append("}\n");

         //
         writer.append("public static final TemplateDescriptor DESCRIPTOR = new TemplateDescriptor(").append(template.getFQN().getFullName()).append(".class);\n");

         //
         String baseBuilderName = Tools.getImport(org.juzu.template.Template.Builder.class);
         if (template.getParameters() != null)
         {
            // Implement abstract method with this class Builder covariant return type
            writer.append("public Builder with() {\n");
            writer.append("return new Builder();\n");
            writer.append("}\n");

            // Setters on builders
            writer.append("public class Builder extends ").append(baseBuilderName).append("\n");
            writer.append("{\n");
            for (String paramName : template.getParameters())
            {
               writer.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
               writer.append("set(\"").append(paramName).append("\",").append(paramName).append(");\n");
               writer.append("return this;\n");
               writer.append(("}\n"));
            }
            writer.append("}\n");
         }
         else
         {
            // Implement abstract method
            writer.append("public ").append(baseBuilderName).append(" with() {\n");
            writer.append("return new ").append(baseBuilderName).append("();\n");
            writer.append("}\n");
         }

         // Close class
         writer.append("}\n");

         //
         classCache.put(template.getFQN(), classFile);

         //
         log.log("Generated template class " + template.getFQN().getFullName() + " as " + classFile.toUri() +
            " with originating elements " + Arrays.asList(elements));
      }
      catch (IOException e)
      {
         throw new CompilationException(e, elements[0], MetaModelErrorCode.CANNOT_WRITE_TEMPLATE_CLASS, template.getPath());
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }

   private void resolveStub(Template template, TemplatePlugin plugin, ProcessingContext context, Element[] elements)
   {
      if (stubCache.containsKey(template.getFQN()))
      {
         log.log("Template strub " + template.getFQN() + " was found in cache");
         return;
      }

      //
      FQN stubFQN = new FQN(template.getFQN().getFullName() + "_");
      TemplateProvider provider = plugin.providers.get(template.getExtension());
      Writer writer = null;
      try
      {
         // Template stub
         JavaFileObject stubFile = context.createSourceFile(stubFQN.getFullName(), elements);
         writer = stubFile.openWriter();
         writer.append("package ").append(stubFQN.getPackageName()).append(";\n");
         writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
         writer.append("@Generated({\"").append(stubFQN.getFullName()).append("\"})\n");
         writer.append("public class ").append(stubFQN.getSimpleName()).append(" extends ").append(provider.getTemplateStubType().getName()).append(" {\n");
         writer.append("}");

         //
         stubCache.put(template.getFQN(), stubFile);

         //
         log.log("Generating template stub " + stubFQN.getFullName() + " as " + stubFile.toUri() +
            " with originating elements " + Arrays.asList(elements));
      }
      catch (IOException e)
      {
         throw new CompilationException(e, elements[0], MetaModelErrorCode.CANNOT_WRITE_TEMPLATE_STUB, template.getPath());
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }
}
