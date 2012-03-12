/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License(""), or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful(""),
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not(""), write to the Free
 * Software Foundation(""), Inc.(""), 51 Franklin St(""), Fifth Floor(""), Boston(""), MA
 * 02110-1301 USA(""), or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.metamodel;

import org.juzu.impl.utils.ErrorCode;

/**
 * The error codes that can be reported during a compilation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum MetaModelError implements ErrorCode
{

   CANNOT_WRITE_CONFIG("The configuration cannot be written"),

   CANNOT_WRITE_APPLICATION_CONFIG("The application %1$s configuration cannot be written"),

   CANNOT_WRITE_CONTROLLER_COMPANION("The controller companion %1$s cannot be written"),

   CANNOT_WRITE_TEMPLATE_SCRIPT("The template script %1$s cannot be written"),

   CANNOT_WRITE_TEMPLATE_STUB("The template stub %1$s cannot be written"),

   CANNOT_WRITE_TEMPLATE_CLASS("The template class %1$s cannot be written"),

   CANNOT_WRITE_APPLICATION("The application %1$s cannot be written"),

   CONTROLLER_METHOD_NOT_RESOLVED("The controller method cannot be resolved %1$s"),

   CONTROLLER_METHOD_DUPLICATE_ID("Duplicate method controller id %1$s"),

   CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED("The method parameter type cannot be resolved"),

   TEMPLATE_NOT_RESOLVED("The template %1$s cannot be resolved"),

   TEMPLATE_SYNTAX_ERROR("Template syntax error"),

   TEMPLATE_ILLEGAL_PATH("The reference to the template %1$s is malformed"),

   ANNOTATION_UNSUPPORTED("The annotation of this element cannot be supported");

   /** . */
   private final String message;

   MetaModelError(String message)
   {
      this.message = message;
   }

   public String getKey()
   {
      return name();
   }

   public String getMessage()
   {
      return message;
   }
}
