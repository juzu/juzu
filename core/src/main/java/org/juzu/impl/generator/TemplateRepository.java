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

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.metamodel.TemplateMetaModel;
import org.juzu.impl.processor.ErrorCode;
import org.juzu.impl.utils.Content;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The template repository.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateRepository implements Serializable
{

   /** . */
   private Set<TemplateMetaModel> toAdd = new HashSet<TemplateMetaModel>();

   /** . */
   private Set<TemplateMetaModel> toRemove = new HashSet<TemplateMetaModel>();

   /** . */
   private Map<String, TemplateModel> templates = new HashMap<String, TemplateModel>();

   void addTemplate(TemplateMetaModel template)
   {
      toRemove.remove(template);
      toAdd.add(template);
   }

   void removeTemplate(TemplateMetaModel template)
   {
      toAdd.remove(template);
      toRemove.add(template);
   }

   List<TemplateModel> resolve(ProcessingEnvironment env) throws CompilationException
   {
      Filer filer = env.getFiler();

      //
      TemplateResolver resolver = new TemplateResolver(filer);

      // Scan existing templates to find if they have been modified
      for (Iterator<TemplateModel> i = templates.values().iterator();i.hasNext();)
      {
         TemplateModel template = i.next();

         //
         Content content = resolver.resolve(template.getFQN(), template.getExtension());

         //
         if (content == null)
         {
            throw new UnsupportedOperationException("todo");
         }
         else if (content.getLastModified() > template.getLastModified())
         {
            i.remove();
         }
      }

      //
      List<TemplateModel> added = new ArrayList<TemplateModel>();

      // Now analyse the templates
      for (TemplateMetaModel template : toAdd)
      {
         TemplateCompiler compiler = new TemplateCompiler(
            template,
            templates,
            env
         );

         //
         try
         {
            added.addAll(compiler.resolve());
         }
         catch (IOException e)
         {
            throw new CompilationException(e, template.getRefs().iterator().next().getHandle().get(env), ErrorCode.TEMPLATE_NOT_FOUND);
         }
      }

      //
      return added;
   }
}
