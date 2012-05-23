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
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ErrorCode
{

   /** . */
   private static final ConcurrentHashMap<String, ErrorCode> codes = new ConcurrentHashMap<String, ErrorCode>();

   /**
    * Decode the error key and return a corresponding error code object. If no error can be decoded
    * for the specified key, null is returned.
    *
    * @param key the error key
    * @return the corresponding error
    */
   public static ErrorCode decode(String key)
   {
      return codes.get(key);
   }

   /** . */
   private final String key;

   /** . */
   private final String message;

   public ErrorCode(String key, String message)
   {
      codes.put(key, this);

      //
      this.key = key;
      this.message = message;
   }

   /**
    * The error key.
    *
    * @return the error key
    */
   public String getKey()
   {
      return key;
   }

   /**
    * The error message.
    *
    * @return the error message
    */
   public String getMessage()
   {
      return message;
   }

   public CompilationException failure(Object... args)
   {
      return new CompilationException(this, args);
   }

   public CompilationException failure(Element element, Object... args)
   {
      return new CompilationException(element, this, args);
   }

   public CompilationException failure(Element element, AnnotationMirror annotation, Object... args)
   {
      return new CompilationException(element, annotation, this, args);
   }
}
