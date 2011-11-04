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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ElementHandle implements Serializable
{

   public static ElementHandle create(Element elt)
   {
      ElementKind kind = elt.getKind();
      switch (kind)
      {
         case FIELD:
         {
            VariableElement variableElt = (VariableElement)elt;
            TypeElement typeElt = (TypeElement)variableElt.getEnclosingElement();
            String name = variableElt.getSimpleName().toString();
            String fqn = typeElt.getQualifiedName().toString();
            return new Field(fqn, name);
         }
         case CLASS:
         {
            TypeElement typeElt = (TypeElement)elt;
            String fqn = typeElt.getQualifiedName().toString();
            return new Class(fqn);
         }
         case PACKAGE:
         {
            PackageElement packageElt = (PackageElement)elt;
            return new Package(packageElt.getQualifiedName().toString());
         }
         default:
            throw new UnsupportedOperationException("Element " + elt + " with kind " + kind + " not supported");
      }
   }

   public abstract Element get(ProcessingEnvironment env);

   private static class Package extends ElementHandle
   {

      /** . */
      private final String fqn;

      private Package(String fqn)
      {
         this.fqn = fqn;
      }

      @Override
      public Element get(ProcessingEnvironment env)
      {
         return env.getElementUtils().getPackageElement(fqn);
      }
   }

   private static class Class extends ElementHandle
   {

      /** . */
      private final String fqn;

      private Class(String fqn)
      {
         this.fqn = fqn;
      }

      @Override
      public Element get(ProcessingEnvironment env)
      {
         return env.getElementUtils().getTypeElement(fqn);
      }
   }

   private static class Field extends ElementHandle
   {

      /** . */
      private final String fqn;

      /** . */
      private final String fieldName;

      private Field(String fqn, String fieldName)
      {
         this.fqn = fqn;
         this.fieldName = fieldName;
      }

      @Override
      public Element get(ProcessingEnvironment env)
      {
         TypeElement typeElt = env.getElementUtils().getTypeElement(fqn);
         for (VariableElement variableElt : ElementFilter.fieldsIn(typeElt.getEnclosedElements()))
         {
            if (variableElt.getSimpleName().contentEquals(fieldName))
            {
               return variableElt;
            }
         }
         return null;
      }
   }
}
