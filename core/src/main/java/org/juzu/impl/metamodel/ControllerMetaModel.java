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

package org.juzu.impl.metamodel;

import org.juzu.Phase;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.processor.ElementHandle;
import org.juzu.impl.processor.ErrorCode;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModel extends MetaModelObject
{

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
   }

   public Map<String, ?> toJSON()
   {
      HashMap<String, Object> json = new HashMap<String, Object>();
      json.put("handle", handle);
      ArrayList<Map<String, ?>> foo = new ArrayList<Map<String, ?>>();
      for (MethodMetaModel method : methods.values())
      {
         foo.add(method.toJSON());
      }
      json.put("methods", foo);
      json.put("application", application == null ? null : application.handle);
      return json;
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

      //
      if (id == null)
      {
         // Temporary
         id = "method_" + Math.abs(new Random().nextInt());
      }

      //
      for (Phase phase : Phase.values())
      {
         if (phase.annotation.getSimpleName().equals(annotationName))
         {
            ArrayList<String> parameterTypes = new ArrayList<String>();
            for (TypeMirror parameterType : ((ExecutableType)methodElt.asType()).getParameterTypes())
            {
               TypeMirror erasedParameterType = context.env.getTypeUtils().erasure(parameterType);
               parameterTypes.add(erasedParameterType.toString());
            }
            ArrayList<String> parameterNames = new ArrayList<String>();
            for (VariableElement variableElt : methodElt.getParameters())
            {
               parameterNames.add(variableElt.getSimpleName().toString());
            }

            // Validate duplicate id within the same controller
            for (MethodMetaModel existing : methods.values())
            {
               if (existing.id.equals(id))
               {
                  throw new CompilationException(methodElt, ErrorCode.DUPLICATE_CONTROLLER_ID, id);
               }
            }

            //
            ElementHandle.Method origin = ElementHandle.Method.create(methodElt);
            MethodMetaModel method = methods.remove(origin);
            if (method != null)
            {
               context.queue.remove(new MetaModelEvent.AddObject(method));
            }
            method = new MethodMetaModel(
               origin,
               this,
               id,
               phase,
               methodElt.getSimpleName().toString(),
               parameterTypes,
               parameterNames);
            methods.put(origin, method);
            context.queue.add(new MetaModelEvent.AddObject(method));
            break;
         }
      }
   }
}
