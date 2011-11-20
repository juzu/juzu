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

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.ParseException;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.utils.Spliterator;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateCompiler extends TemplateCompilationContext
{

   /** . */
   private final ApplicationModel application;

   /** . */
   private Foo foo;

   /** . */
   private final MainProcessor processor;

   /** . */
   private final ArrayList<TemplateModel> added;

   /**
    * We need two locations as the {@link StandardLocation#SOURCE_PATH} is not supported in eclipse ide
    * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341298), however the {@link StandardLocation#CLASS_OUTPUT}
    * seems to work fairly well.
    */
   private static final StandardLocation[] locations = { StandardLocation.SOURCE_PATH, StandardLocation.CLASS_OUTPUT};

   public TemplateCompiler(ApplicationModel application, Foo foo, MainProcessor processor)
   {
      this.application = application;
      this.foo = foo;
      this.processor = processor;
      this.added = new ArrayList<TemplateModel>();
   }

   public Iterable<TemplateModel> getAdded()
   {
      return added;
   }

   @Override
   public void resolveTemplate(String path) throws IOException
   {
      TemplateModel template = application.templates.get(path);

      //
      if (template == null)
      {
         Foo foo = new Foo(this.foo, path);

         // Resolve the template fqn and the template name
         String fqn = application.templatesFQN;
         for (String name: Spliterator.split(foo.getFolder() + foo.getRawName(), '/'))
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
         CharSequence content = null;
         Exception exception = null;
         for (StandardLocation location : locations)
         {
            try
            {
               String pkg = stubFQN.getPackageName().getValue();
               String relativeName = stubFQN.getSimpleName() + "." + foo.getExtension();
               MainProcessor.log("Attempt to obtain template " + pkg + " " + relativeName + " from " + location.getName());
               FileObject resource = processor.filer.getResource(location, pkg, relativeName);
               content = resource.getCharContent(true);
               if (content != null)
               {
                  MainProcessor.log("Obtained template " + resource.toUri() + " from " + location.getName());
                  break;
               }
            }
            catch (Exception e)
            {
               exception = e;
            }
         }

         //
         if (content == null)
         {
            throw new CompilationException(exception, processor.get(foo.getOrigin()), ErrorCode.TEMPLATE_NOT_FOUND, stubFQN);
         }

         // Parse to AST
         ASTNode.Template templateAST;
         try
         {
            templateAST = ASTNode.Template.parse(content);
         }
         catch (ParseException e)
         {
            throw new CompilationException(processor.get(foo.getOrigin()), ErrorCode.TEMPLATE_SYNTAX_ERROR, fqn);
         }

         // Obtain template parameters
         ArrayList<ASTNode.Tag> paramTags = new ArrayList<ASTNode.Tag>();
         collectParams(templateAST, paramTags);
         LinkedHashSet<String> parameters = null;
         if (paramTags.size() > 0)
         {
            parameters = new LinkedHashSet<String>();
            for (ASTNode.Tag paramTag : paramTags)
            {
               String paramName = paramTag.getArgs().get("name");
               parameters.add(paramName);
            }
         }

         // Add template to application
         application.templates.put(path, template = new TemplateModel(foo, templateAST, stubFQN, parameters));

         // Process template
         templateAST.process(this);

         //
         added.add(template);
      }
   }

   private void collectParams(ASTNode<?> node, List<ASTNode.Tag> tags)
   {
      for (ASTNode.Block child : node.getChildren())
      {
         collectParams(child, tags);
      }
      if (node instanceof ASTNode.Tag)
      {
         ASTNode.Tag tag = (ASTNode.Tag)node;
         if (tag.getName().equals("param"))
         {
            tags.add(tag);
         }
      }
   }

   @Override
   public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
   {
      throw new UnsupportedOperationException();
   }
}
