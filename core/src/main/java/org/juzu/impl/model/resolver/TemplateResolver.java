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

package org.juzu.impl.model.resolver;

import org.juzu.Path;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.inject.Export;
import org.juzu.impl.model.meta.ApplicationMetaModel;
import org.juzu.impl.model.meta.MethodMetaModel;
import org.juzu.impl.model.meta.TemplateMetaModel;
import org.juzu.impl.model.meta.TemplateRefMetaModel;
import org.juzu.impl.processor.ElementHandle;
import org.juzu.impl.processor.ErrorCode;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.utils.Tools;
import org.juzu.request.ApplicationContext;

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
   private Map<String, FileObject> resources;
   
   public TemplateResolver(ApplicationMetaModel application)
   {
      this.application = application;
      this.templates = new HashMap<String, Template>();
      this.resources = new HashMap<String, FileObject>();
   }

   public Collection<Template> getTemplates()
   {
      return templates.values();
   }

   void prePassivate()
   {
      log.log("Evicting template cache " + resources.keySet());
      resources.clear();
   }

   void process(ModelResolver context) throws CompilationException
   {
      // Evict templates that are out of date
      log.log("Synchronizing existing templates " + templates.keySet());
      TemplateFiler resolver = new TemplateFiler(context.env.getFiler());
      for (Iterator<Template> i = templates.values().iterator();i.hasNext();)
      {
         Template template = i.next();
         Content content = resolver.resolve(template.getFQN(), template.getExtension());
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
            TemplateCompiler compiler = new TemplateCompiler(templateMeta, new HashMap<String, Template>(copy), context.env);
            List<Template> resolved = compiler.resolve();
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
         TemplateMetaModel templateMeta = application.getTemplate(originPath);

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
            elements[index++] = context.env.getElementUtils().getTypeElement(type.getFullName());
         }

         // Resolve the stub
         resolveStub(template, context, elements);

         // Resolve the qualified class
         resolvedQualified(template, context, elements);

         //
         resolveScript(template, context, elements);
      }
   }

   private void resolveScript(Template template, ModelResolver context, final Element[] elements)
   {
      TemplateProvider provider = context.providers.get(template.getExtension());

      // If it's the cache we do nothing
      String key = template.getFQN().getFullName() + ".groovy";
      if (resources.containsKey(key))
      {
         log.log("Template " + key + " was found in cache");
         return;
      }

      // Attempt to get the script to check it's generated
      try
      {
         FileObject scriptFile = context.env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, template.getFQN().getPackageName(), template.getFQN().getSimpleName() + "." + provider.getTargetExtension());
         scriptFile.getCharContent(true);
         log.log("Template " + key + " was found on disk cache");
         resources.put(key, scriptFile);
         return;
      }
      catch (IOException e)
      {
         log.log("Template " + key + " was not found on disk");
      }

      //
      Writer writer = null;
      try
      {
         TemplateGenerator generator = provider.newGenerator();
         ASTNode.Template ast = template.getAST();
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
         FileObject scriptFile = context.env.getFiler().createResource(StandardLocation.CLASS_OUTPUT, template.getFQN().getPackageName(), template.getFQN().getSimpleName() + "." + provider.getTargetExtension(), elements);
         writer = scriptFile.openWriter();
         writer.write(generator.toString());

         // Put it in cache
         resources.put(key, scriptFile);

         //
         log.log("Generated template script " + template.getFQN().getFullName() + " as " + scriptFile.toUri() +
            " with originating elements " + Arrays.asList(elements));
      }
      catch (IOException e)
      {
         throw new CompilationException(e, elements[0], ErrorCode.CANNOT_WRITE_TEMPLATE);
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }

   private void resolvedQualified(Template template, ModelResolver context, Element[] elements)
   {
      if (context.env.getElementUtils().getTypeElement(template.getFQN().getFullName()) == null)
      {
         Writer writer = null;
         try
         {
            // Template qualified class
            FileObject classFile = context.env.getFiler().createSourceFile(template.getFQN().getFullName(), elements);
            writer = classFile.openWriter();
            writer.append("package ").append(template.getFQN().getPackageName()).append(";\n");
            writer.append("import ").append(Tools.getImport(Path.class)).append(";\n");
            writer.append("import ").append(Tools.getImport(Export.class)).append(";\n");
            writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
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
            if (template.getParameters() != null)
            {
               // Setters on template
               for (String paramName : template.getParameters())
               {
                  writer.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
                  writer.append("Builder builder = new Builder();");
                  writer.append("builder.set(\"").append(paramName).append("\",").append(paramName).append(");\n");
                  writer.append("return builder;\n");
                  writer.append(("}\n"));
               }

               // Setters on builders
               writer.append("public class Builder extends ").append(Tools.getImport(org.juzu.template.Template.Builder.class)).append("\n");
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

            // Close class
            writer.append("}\n");

            //
            log.log("Generated template class " + template.getFQN().getFullName() + " as " + classFile.toUri() +
               " with originating elements " + Arrays.asList(elements));
         }
         catch (IOException e)
         {
            throw new CompilationException(e, elements[0], ErrorCode.CANNOT_WRITE_TEMPLATE_QUALIFIED_CLASS, template.getPath());
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
      else
      {
         log.log("Found existing qualified template " + template.getFQN().getFullName());
      }
   }

   private void resolveStub(Template template, ModelResolver context, Element[] elements)
   {
      FQN stubFQN = new FQN(template.getFQN().getFullName() + "_");

      //
      if (context.env.getElementUtils().getTypeElement(stubFQN.getFullName()) == null)
      {
         TemplateProvider provider = context.providers.get(template.getExtension());
         Writer writer = null;
         try
         {
            // Template stub
            JavaFileObject stubFile = context.env.getFiler().createSourceFile(stubFQN.getFullName(), elements);
            writer = stubFile.openWriter();
            writer.append("package ").append(stubFQN.getPackageName()).append(";\n");
            writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
            writer.append("@Generated({\"").append(stubFQN.getFullName()).append("\"})\n");
            writer.append("public class ").append(stubFQN.getSimpleName()).append(" extends ").append(provider.getTemplateStubType().getName()).append(" {\n");
            writer.append("}");

            //
            log.log("Generating template stub " + stubFQN.getFullName() + " as " + stubFile.toUri() +
               " with originating elements " + Arrays.asList(elements));
         }
         catch (IOException e)
         {
            throw new CompilationException(e, elements[0], ErrorCode.CANNOT_WRITE_TEMPLATE_STUB_CLASS, template.getPath());
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
      else
      {
         log.log("Found existing template stub " + stubFQN.getFullName());
      }
   }
}
