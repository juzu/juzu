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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends RuntimeException
{

   /** . */
   private final MessageCode code;

   /** . */
   private Object[] arguments;

   /** . */
   private final Element element;

   /** . */
   private final AnnotationMirror annotation;

   public CompilationException(MessageCode code, Object... arguments)
   {
      this(null, code, arguments);
   }

   public CompilationException(Element element, MessageCode code, Object... arguments)
   {
      this(element, null, code, arguments);
   }

   public CompilationException(Element element, AnnotationMirror annotation, MessageCode code, Object... arguments)
   {
      this.code = code;
      this.element = element;
      this.arguments = arguments;
      this.annotation = annotation;
   }

   @Override
   public synchronized CompilationException initCause(Throwable cause)
   {
      return (CompilationException)super.initCause(cause);
   }

   public Element getElement()
   {
      return element;
   }

   public AnnotationMirror getAnnotation()
   {
      return annotation;
   }

   public MessageCode getCode()
   {
      return code;
   }

   public Object[] getArguments()
   {
      return arguments;
   }
}
