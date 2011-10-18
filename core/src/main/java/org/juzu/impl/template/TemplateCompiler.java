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

package org.juzu.impl.template;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.application.ApplicationProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.utils.Spliterator;
import org.juzu.impl.utils.Tools;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TemplateCompiler 
{

   /** . */
   private static final Pattern NAME_PATTERN = Pattern.compile("([^.]+)\\.([a-zA-Z]+)");

   /** . */
   private TemplateProcessor processor;

   /** . */
   private final ApplicationProcessor.ApplicationMetaData application;

   /** . */
   private final Filer filer;

   /** . */
   private final String templatesPkgFQN;

   /** . */
   private final Map<String, String> cache;

   /** The templates built in progress. */
   private final Set<String> building;

   TemplateCompiler(
      TemplateProcessor processor,
      ApplicationProcessor.ApplicationMetaData application,
      Filer filer)
   {
      StringBuilder templatesPkgSB = new StringBuilder(application.getPackageName());
      if (templatesPkgSB.length() > 0)
      {
         templatesPkgSB.append(".");
      }
      templatesPkgSB.append("templates");

      //
      this.templatesPkgFQN = templatesPkgSB.toString();
      this.application = application;
      this.filer = filer;
      this.processor = processor;
      this.cache = new HashMap<String, String>();
      this.building = new HashSet<String>();
   }
   
   String compile(final Element element, String path) throws IOException
   {
      String v = cache.get(path);
      if (v != null)
      {
         return v;
      }
      if (building.contains(path))
      {
         throw new UnsupportedOperationException("circulariry detected (not handled for now)");
      }

      // Resolve the template fqn and the template name
      Spliterator s = new Spliterator(path, '/');
      String templatePkgFQN = templatesPkgFQN;
      String templateName = null;
      while (s.hasNext())
      {
         if (templateName != null)
         {
            if (templatePkgFQN.length() == 0)
            {
               templatePkgFQN = templateName;
            }
            else
            {
               templatePkgFQN = templatePkgFQN + "." +  templateName;
            }
         }
         templateName = s.next();
      }

      //
      Matcher matcher = NAME_PATTERN.matcher(templateName);
      if (!matcher.matches())
      {
         throw new UnsupportedOperationException("wrong template path " + path);
      }

      //
      FileObject file = filer.getResource(StandardLocation.SOURCE_PATH, templatePkgFQN, templateName);
      CharSequence content = file.getCharContent(false).toString();

      //
      String extension = matcher.group(2);
      TemplateProvider provider = processor.providers.get(extension);

      //
      TemplateCompilationContext tgc = new TemplateCompilationContext()
      {
         @Override
         public String resolveTemplate(String path) throws IOException
         {
            return compile(element, path);
         }
         @Override
         public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
         {
            ApplicationProcessor.MethodMetaData methodMD;
            try
            {
               methodMD = application.resolve(typeName, methodName, parameterMap.keySet());
            }
            catch (AmbiguousResolutionException e)
            {
               throw new CompilationException(element, "Could not resolve method arguments " + methodName + parameterMap);
            }
            if (methodMD != null)
            {
               List<String> args = new ArrayList<String>();
               for (VariableElement ve : methodMD.getElement().getParameters())
               {
                  String value = parameterMap.get(ve.getSimpleName().toString());
                  args.add(value);
               }
               return new MethodInvocation(methodMD.getController().getClassName() + "_", methodMD.getName() + "URL", args);
            }
            else
            {
               throw new CompilationException(element, "Could not resolve method name " + methodName + parameterMap);
            }
         }
      };

      //
      if (provider != null)
      {
         TemplateGenerator generator = provider.newGenerator();

         try
         {
            ASTNode.Template.parse(content).generate(generator, tgc);
            building.add(path);
            String ret = generator.generate(filer, templatePkgFQN, matcher.group(1));
            cache.put(path, ret);
            return ret;
         }
         catch (ParseException e)
         {
            throw new CompilationException(element, "Could not compile template " + path, e);
         }
         finally
         {
            building.remove(path);
         }
      }
      else
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
   }
}
