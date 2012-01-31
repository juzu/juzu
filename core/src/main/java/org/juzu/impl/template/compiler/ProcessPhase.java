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

package org.juzu.impl.template.compiler;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.template.ASTNode;
import org.juzu.template.TagHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessPhase extends CompilationPhase
{

   /** . */
   private final Map<String, Template> templates;

   /** . */
   private final ProcessContext context;

   public ProcessPhase(ProcessContext context, Map<String, Template> templates)
   {
      this.templates = templates;
      this.context = context;
   }

   public Map<String, Template> getTemplates()
   {
      return templates;
   }

   /** . */
   private String originPath;

   /** . */
   private List<Template> added;

   public Collection<Template> resolveTemplates(String originPath)
   {
      this.added = new ArrayList<Template>();
      this.originPath = originPath;
      
      //
      resolveTemplate(originPath);
      
      //
      return added;
   }

   public void resolveTemplate(String path) throws CompilationException
   {
      Template template = templates.get(path);

      //
      if (template == null)
      {

         //
         templates.put(path, template);

         //
         template = context.resolveTemplate(originPath, path);

         //
         ASTNode.Template templateAST = template.getAST();

         // Process template
         doAttribute(templateAST);
         doProcess(templateAST);
         doResolve(templateAST);
         doUnattribute(templateAST);

         //
         added.add(template);
      }
   }

   private void doProcess(ASTNode<?> node) throws CompilationException
   {
      if (node instanceof ASTNode.Template)
      {
         for (ASTNode.Block child : node.getChildren())
         {
            doProcess(child);
         }
      }
      else if (node instanceof ASTNode.Section)
      {
         // Do nothing
      }
      else if (node instanceof ASTNode.URL)
      {
         // Do nothing
      }
      else if (node instanceof ASTNode.Tag)
      {
         ASTNode.Tag nodeTag = (ASTNode.Tag)node;
         TagHandler handler = get(nodeTag);
         if (handler instanceof ExtendedTagHandler)
         {
            ((ExtendedTagHandler)handler).process(nodeTag);
         }
         for (ASTNode.Block child : nodeTag.getChildren())
         {
            doProcess(child);
         }
      }
   }

   private void doResolve(ASTNode<?> node) throws CompilationException
   {
      if (node instanceof ASTNode.Template)
      {
         for (ASTNode.Block child : node.getChildren())
         {
            doResolve(child);
         }
      }
      else if (node instanceof ASTNode.Section)
      {
         // Do nothing
      }
      else if (node instanceof ASTNode.URL)
      {
         // Do nothing
      }
      else if (node instanceof ASTNode.Tag)
      {
         ASTNode.Tag nodeTag = (ASTNode.Tag)node;
         TagHandler handler = get(nodeTag);
         if (handler instanceof ExtendedTagHandler)
         {
            ((ExtendedTagHandler)handler).compile(this, nodeTag.getArgs());
         }
         for (ASTNode.Block child : nodeTag.getChildren())
         {
            doResolve(child);
         }
      }
   }
}
