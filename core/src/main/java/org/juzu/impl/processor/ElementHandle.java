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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ElementHandle<E extends Element> implements Serializable
{

   public static ElementHandle.Field create(VariableElement elt)
   {
      TypeElement typeElt = (TypeElement)elt.getEnclosingElement();
      String name = elt.getSimpleName().toString();
      String fqn = typeElt.getQualifiedName().toString();
      return new Field(fqn, name);
   }

   public static Class create(TypeElement elt)
   {
      return new Class(elt.getQualifiedName().toString());
   }

   public static Package create(PackageElement elt)
   {
      return new Package(elt.getQualifiedName().toString());
   }

   public static Method create(ExecutableElement elt)
   {
      TypeElement typeElt = (TypeElement)elt.getEnclosingElement();
      String name = elt.getSimpleName().toString();
      String fqn = typeElt.getQualifiedName().toString();
      ArrayList<String> parameterTypes = new ArrayList<String>();
      for (TypeMirror parameterType : ((ExecutableType)elt.asType()).getParameterTypes())
      {
         parameterTypes.add(parameterType.toString());
      }

      return new Method(fqn, name, parameterTypes);
   }

   public static ElementHandle<?> create(Element elt)
   {
      ElementKind kind = elt.getKind();
      switch (kind)
      {
         case FIELD:
         {
            VariableElement variableElt = (VariableElement)elt;
            return create(variableElt);
         }
         case CLASS:
         {
            TypeElement typeElt = (TypeElement)elt;
            return create(typeElt);
         }
         case PACKAGE:
         {
            PackageElement packageElt = (PackageElement)elt;
            return create(packageElt);
         }
         case METHOD:
         {
            ExecutableElement packageElt = (ExecutableElement)elt;
            return create(packageElt);
         }
         default:
            throw new UnsupportedOperationException("Element " + elt + " with kind " + kind + " not supported");
      }
   }

   public abstract E get(ProcessingEnvironment env);

   public abstract boolean equals(Object obj);

   public abstract int hashCode();

   public static class Package extends ElementHandle<PackageElement>
   {

      /** . */
      private final String fqn;

      private Package(String fqn)
      {
         this.fqn = fqn;
      }

      @Override
      public PackageElement get(ProcessingEnvironment env)
      {
         return env.getElementUtils().getPackageElement(fqn);
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         else if (obj instanceof Package)
         {
            Package that = (Package)obj;
            return fqn.equals(that.fqn);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return fqn.hashCode();
      }
   }

   public static class Class extends ElementHandle<TypeElement>
   {

      /** . */
      private final String fqn;

      private Class(String fqn)
      {
         this.fqn = fqn;
      }

      @Override
      public TypeElement get(ProcessingEnvironment env)
      {
         return env.getElementUtils().getTypeElement(fqn);
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         else if (obj instanceof Class)
         {
            Class that = (Class)obj;
            return fqn.equals(that.fqn);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return fqn.hashCode();
      }
   }

   public static class Method extends ElementHandle<ExecutableElement>
   {

      /** . */
      private final String fqn;

      /** . */
      private final String name;

      /** . */
      private final ArrayList<String> parameterTypes;

      public Method(String fqn, String name, ArrayList<String> parameterTypes)
      {
         this.fqn = fqn;
         this.name = name;
         this.parameterTypes = parameterTypes;
      }

      @Override
      public ExecutableElement get(ProcessingEnvironment env)
      {
         TypeElement typeElt = env.getElementUtils().getTypeElement(fqn);
         next:
         for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElt.getEnclosedElements()))
         {
            if (executableElement.getSimpleName().toString().equals(name))
            {
               List<? extends TypeMirror> parameterTypes = ((ExecutableType)executableElement.asType()).getParameterTypes();
               int len = parameterTypes.size();
               if (len == this.parameterTypes.size())
               {
                  for (int i = 0;i < len;i++)
                  {
                     if (!parameterTypes.get(i).toString().equals(this.parameterTypes.get(i)))
                     {
                        continue next;
                     }
                  }
               }
               return executableElement;
            }
         }
         return null;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         else if (obj instanceof Method)
         {
            Method that = (Method)obj;
            return fqn.equals(that.fqn) && name.equals(that.name) && parameterTypes.equals(that.parameterTypes);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         int hashCode = fqn.hashCode() ^ name.hashCode();
         for (String parameterType : parameterTypes)
         {
            hashCode = hashCode * 41 + parameterType.hashCode();
         }
         return hashCode;
      }
   }

   public static class Field extends ElementHandle<VariableElement>
   {

      /** . */
      private final String fqn;

      /** . */
      private final String name;

      private Field(String fqn, String name)
      {
         this.fqn = fqn;
         this.name = name;
      }

      @Override
      public VariableElement get(ProcessingEnvironment env)
      {
         TypeElement typeElt = env.getElementUtils().getTypeElement(fqn);
         for (VariableElement variableElt : ElementFilter.fieldsIn(typeElt.getEnclosedElements()))
         {
            if (variableElt.getSimpleName().contentEquals(name))
            {
               return variableElt;
            }
         }
         return null;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         else if (obj instanceof Field)
         {
            Field that = (Field)obj;
            return fqn.equals(that.fqn) && name.equals(that.name);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return fqn.hashCode() ^ name.hashCode();
      }
   }
}
