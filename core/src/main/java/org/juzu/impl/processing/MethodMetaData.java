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

package org.juzu.impl.processing;

import org.juzu.Phase;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import java.util.LinkedHashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class MethodMetaData
{

   /** . */
   final String id;

   /** . */
   final ControllerMetaData controller;

   /** . */
   final Phase phase;

   /** . */
   final ExecutableElement element;

   /** . */
   final ExecutableType type;

   /** . */
   final LinkedHashSet<String> parameterNames;

   MethodMetaData(ControllerMetaData controller, String id, Phase phase, ExecutableElement element)
   {
      LinkedHashSet<String> parameterNames = new LinkedHashSet<String>();
      for (VariableElement variableElt : element.getParameters())
      {
         parameterNames.add(variableElt.getSimpleName().toString());
      }

      //
      this.id = id;
      this.controller = controller;
      this.phase = phase;
      this.element = element;
      this.type = (ExecutableType)element.asType();
      this.parameterNames = parameterNames;
   }

   public String getName()
   {
      return element.getSimpleName().toString();
   }
}
