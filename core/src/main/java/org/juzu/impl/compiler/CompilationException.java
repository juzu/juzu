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

import javax.lang.model.element.Element;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends RuntimeException
{

   /** . */
   private final Element element;

   public CompilationException(String message)
   {
      super(message);

      //
      this.element = null;
   }

   public CompilationException(String message, Throwable cause)
   {
      super(message, cause);

      //
      this.element = null;
   }

   public CompilationException(Throwable cause)
   {
      super(cause);

      //
      this.element = null;
   }

   public CompilationException(Element element, String message)
   {
      super(message);

      //
      this.element = element;
   }

   public CompilationException(Element element, String message, Throwable cause)
   {
      super(message, cause);

      //
      this.element = element;
   }

   public Element getElement()
   {
      return element;
   }
}
