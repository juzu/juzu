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

package org.juzu.impl.compiler;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A processor plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ProcessorPlugin
{

   Processor processor;

   public void init()
   {
   }

   public void process() throws CompilationException
   {
   }

   public void over() throws CompilationException
   {
   }

   public void destroy()
   {
   }

   protected final <P extends ProcessorPlugin> P getPlugin(Class<P> pluginType)
   {
      for (ProcessorPlugin plugin : processor.plugins)
      {
         if (pluginType.isInstance(plugin))
         {
            return pluginType.cast(plugin);
         }
      }
      return null;
   }

   protected final Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a)
   {
      return processor.roundEnv.getElementsAnnotatedWith(a);
   }

   protected final PackageElement getPackageOf(Element elt)
   {
      return processor.processingEnv.getElementUtils().getPackageOf(elt);
   }

   protected final FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) throws IOException
   {
      return getFiler().getResource(location, pkg, relativeName);
   }

   protected final JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException
   {
      return getFiler().createSourceFile(name, originatingElements);
   }

   protected final FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException
   {
      return getFiler().createResource(location, pkg, relativeName, originatingElements);
   }

   protected final Filer getFiler()
   {
      return processor.processingEnv.getFiler();
   }

   protected final TypeMirror erasure(TypeMirror t)
   {
      return processor.processingEnv.getTypeUtils().erasure(t);
   }
}
