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

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateCompiler extends TemplateCompilationContext
{

   /** . */
   private final ApplicationModel application;

   /** . */
   private Foo foo;

   /** . */
   private final ProcessingEnvironment env;

   /** . */
   private final ArrayList<TemplateModel> added;

   public TemplateCompiler(ApplicationModel application, Foo foo, ProcessingEnvironment env)
   {
      this.application = application;
      this.foo = foo;
      this.env = env;
      this.added = new ArrayList<TemplateModel>();
   }

   private void process(ASTNode.Template template) throws IOException
   {
      template.process(this);
   }

   public Iterable<TemplateModel> getAdded()
   {
      return added;
   }

   @Override
   public String resolveTemplate(String path) throws IOException
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
         CharSequence content;
         try
         {
            FileObject file = env.getFiler().getResource(StandardLocation.SOURCE_PATH, stubFQN.getPackageName(), stubFQN.getSimpleName() + "." + foo.getExtension());
            content = file.getCharContent(false).toString();
         }
         catch (IOException e)
         {
            throw new CompilationException(foo.getOrigin().get(env), "Could not obtain template " + stubFQN, e);
         }

         // Parse to AST
         ASTNode.Template templateAST;
         try
         {
            templateAST = ASTNode.Template.parse(content);
         }
         catch (ParseException e)
         {
            throw new CompilationException(foo.getOrigin().get(env), "Could not parse template " + fqn);
         }

         // Add template to application
         application.templates.put(path, template = new TemplateModel(foo, templateAST, stubFQN));

         // Process template
         try
         {
            process(templateAST);
         }
         catch (IOException e)
         {
            throw new CompilationException(foo.getOrigin().get(env), "Could not process template " + fqn);
         }

         //
         added.add(template);
      }

      //
      return template.getStubFQN().getFullName();
   }

   @Override
   public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
   {
      throw new UnsupportedOperationException();
   }
}
