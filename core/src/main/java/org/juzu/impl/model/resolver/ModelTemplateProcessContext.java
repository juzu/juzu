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

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.CompilationErrorCode;
import org.juzu.impl.model.meta.TemplateMetaModel;
import org.juzu.impl.model.processor.ProcessingContext;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.ParseException;
import org.juzu.impl.template.compiler.Template;
import org.juzu.impl.template.compiler.ProcessContext;
import org.juzu.impl.template.compiler.ProcessPhase;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Spliterator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ModelTemplateProcessContext extends ProcessContext
{

   /** . */
   private TemplateMetaModel templateMetaModel;

   /** . */
   private final ProcessingContext env;

   /** . */
   private final Map<String, Template> templates; 

   ModelTemplateProcessContext(
      TemplateMetaModel templateMetaModel,
      Map<String, Template> templates,
      ProcessingContext env)
   {
      this.templateMetaModel = templateMetaModel;
      this.env = env;
      this.templates = templates;

   }

   Collection<Template> resolve(final TemplateMetaModel metaModel)
   {
      ElementHandle.Field handle = templateMetaModel.getRefs().iterator().next().getHandle();
      return env.executeWithin(handle, new Callable<Collection<Template>>()
      {
         public Collection<Template> call() throws Exception
         {
            Set<String> keys = new HashSet<String>(templates.keySet());
            ProcessPhase phase = new ProcessPhase(ModelTemplateProcessContext.this, templates);
            phase.resolveTemplate(metaModel.getPath());
            Map<String, Template> copy = new HashMap<String, Template>(templates);
            copy.keySet().removeAll(keys);
            return copy.values();
         }
      });
   }

   protected Content resolveResource(FQN fqn, String extension)
   {
      return env.resolveResource(fqn, extension);
   }

   @Override
   protected Template resolveTemplate(String originPath, String path)
   {
      Matcher matcher = ModelResolver.TEMPLATE_PATH_PATTERN.matcher(path);

      //
      if (!matcher.matches())
      {
         throw new CompilationException(CompilationErrorCode.TEMPLATE_ILLEGAL_PATH, path);
      }
      String folder = matcher.group(1);
      String rawName = matcher.group(2);
      String extension = matcher.group(3);

      // Resolve the template fqn and the template name
      String fqn = templateMetaModel.getApplication().getTemplatesQN().getValue();
      for (String name: Spliterator.split(folder + rawName, '/'))
      {
         if (fqn.length() == 0)
         {
            fqn = name;
         }
         else
         {
            fqn += "." +  name;
         }
      }
      FQN stubFQN = new FQN(fqn);

      // Get source
      Content content = resolveResource(stubFQN, extension);
      if (content == null)
      {
         throw new CompilationException(CompilationErrorCode.TEMPLATE_NOT_RESOLVED, fqn);
      }

      // Parse to AST
      ASTNode.Template templateAST;
      try
      {
         templateAST = ASTNode.Template.parse(content.getCharSequence());
      }
      catch (ParseException e)
      {
         throw new CompilationException(CompilationErrorCode.TEMPLATE_SYNTAX_ERROR, path);
      }

      // Add template to application
      return new Template(
         originPath,
         templateAST,
         stubFQN,
         extension,
         path,
         content.getLastModified());
   }
}
