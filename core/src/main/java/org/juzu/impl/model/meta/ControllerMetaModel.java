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

package org.juzu.impl.model.meta;

import org.juzu.impl.utils.JSON;
import org.juzu.request.Phase;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.CompilationErrorCode;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModel extends MetaModelObject
{

   /** A flag for handling modified event. */
   boolean modified;

   /** . */
   final MetaModel context;

   /** The application. */
   ApplicationMetaModel application;

   /** . */
   final ElementHandle.Class handle;

   /** . */
   final LinkedHashMap<ElementHandle.Method, MethodMetaModel> methods;

   public ControllerMetaModel(MetaModel context, ElementHandle.Class handle)
   {
      this.context = context;
      this.handle = handle;
      this.methods = new LinkedHashMap<ElementHandle.Method, MethodMetaModel>();
      this.modified = false;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("handle", handle);
      json.add("methods", methods.values());
      json.add("application", application == null ? null : application.handle);
      return json;
   }

   public ApplicationMetaModel getApplication()
   {
      return application;
   }

   public ElementHandle.Class getHandle()
   {
      return handle;
   }

   public List<MethodMetaModel> getMethods()
   {
      return new ArrayList<MethodMetaModel>(methods.values());
   }

   public MethodMetaModel addMethod(Phase phase, String name, Iterable<Map.Entry<String, String>> parameters)
   {
      ArrayList<String> parameterTypes = new ArrayList<String>();
      ArrayList<String> parameterNames = new ArrayList<String>();
      for (Map.Entry<String, String> entry : parameters)
      {
         parameterTypes.add(entry.getValue());
         parameterNames.add(entry.getKey());
      }
      ElementHandle.Method handle = ElementHandle.Method.create(this.handle.getFQN(), name, parameterTypes);
      if (methods.containsKey(handle))
      {
         throw new IllegalStateException();
      }
      MethodMetaModel method = new MethodMetaModel(
         handle,
         this,
         "" + Math.random(),
         phase,
         name,
         parameterTypes,
         parameterNames);
      methods.put(handle, method);
      return method;
   }

   void addMethod(
      ExecutableElement methodElt,
      String annotationName,
      Map<String, Object> annotationValues)
   {
      String id = (String)annotationValues.get("id");

      // For now we compute an id based on a kind of signature
      if (id == null)
      {
         StringBuilder sb = new StringBuilder();
         sb.append(methodElt.getEnclosingElement().getSimpleName());
         sb.append("_");
         sb.append(methodElt.getSimpleName());
         for (VariableElement ve : methodElt.getParameters())
         {
            sb.append("_").append(ve.getSimpleName());
         }
         id = sb.toString();
      }

      //
      for (Phase phase : Phase.values())
      {
         if (phase.annotation.getSimpleName().equals(annotationName))
         {
            ArrayList<String> parameterTypes = new ArrayList<String>();
            for (TypeMirror parameterType : ((ExecutableType)methodElt.asType()).getParameterTypes())
            {
               TypeMirror erasedParameterType = context.env.erasure(parameterType);
               parameterTypes.add(erasedParameterType.toString());
            }
            ArrayList<String> parameterNames = new ArrayList<String>();
            for (VariableElement variableElt : methodElt.getParameters())
            {
               parameterNames.add(variableElt.getSimpleName().toString());
            }

            //
            ElementHandle.Method origin = ElementHandle.Method.create(methodElt);

            // First remove the previous method
            methods.remove(origin);

            // Validate duplicate id within the same controller
            for (MethodMetaModel existing : methods.values())
            {
               if (existing.id.equals(id))
               {
                  throw new CompilationException(methodElt, CompilationErrorCode.CONTROLLER_METHOD_DUPLICATE_ID, id);
               }
            }

            //
            MethodMetaModel method = new MethodMetaModel(
               origin,
               this,
               id,
               phase,
               methodElt.getSimpleName().toString(),
               parameterTypes,
               parameterNames);
            methods.put(origin, method);
            modified = true;
            break;
         }
      }
   }
}
