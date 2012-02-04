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
import org.juzu.metadata.Cardinality;
import org.juzu.request.Phase;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.CompilationErrorCode;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
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
      ArrayList<Cardinality> parameterCardinalities = new ArrayList<Cardinality>();
      ArrayList<String> parameterNames = new ArrayList<String>();
      for (Map.Entry<String, String> entry : parameters)
      {
         parameterTypes.add(entry.getValue());
         parameterCardinalities.add(Cardinality.SINGLE);
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
         null,
         phase,
         name,
         parameterTypes,
         parameterCardinalities,
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
      for (Phase phase : Phase.values())
      {
         if (phase.annotation.getSimpleName().equals(annotationName))
         {
            List<? extends TypeMirror> parameterTypeMirrors = ((ExecutableType)methodElt.asType()).getParameterTypes();
            List<? extends VariableElement> parameterVariableElements = methodElt.getParameters();

            //
            ArrayList<String> parameterTypes = new ArrayList<String>();
            ArrayList<String> parameterNames = new ArrayList<String>();
            ArrayList<Cardinality> parameterCardinalities = new ArrayList<Cardinality>();
            for (int i = 0;i < parameterTypeMirrors.size();i++)
            {
               VariableElement parameterVariableElement = parameterVariableElements.get(i);
               TypeMirror parameterTypeMirror = parameterTypeMirrors.get(i); 
               TypeMirror erasedParameterTypeMirror = context.env.erasure(parameterTypeMirror);
               parameterTypes.add(erasedParameterTypeMirror.toString());
               TypeMirror parameterSimpleTypeMirror;
               Cardinality cardinality;
               switch (parameterTypeMirror.getKind())
               {
                  case DECLARED:
                     DeclaredType dt = (DeclaredType)parameterTypeMirror;
                     TypeElement col = context.env.getTypeElement("java.util.List");
                     TypeMirror tm = context.env.erasure(col.asType());
                     TypeMirror err = context.env.erasure(dt);
                     // context.env.isSubtype(err, tm)
                     if (err.equals(tm))
                     {
                        if (dt.getTypeArguments().size() != 1)
                        {
                           throw new CompilationException(parameterVariableElement, CompilationErrorCode.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
                        }
                        else
                        {
                           cardinality = Cardinality.LIST;
                           parameterSimpleTypeMirror = dt.getTypeArguments().get(0);
                        }
                     }
                     else
                     {
                        cardinality = Cardinality.SINGLE;
                        parameterSimpleTypeMirror = parameterTypeMirror;
                     }
                     break;
                  case ARRAY:
                     // Unwrap array
                     ArrayType arrayType = (ArrayType)parameterTypeMirror;
                     cardinality = Cardinality.ARRAY;
                     parameterSimpleTypeMirror = arrayType.getComponentType();
                     break;
                  default:
                     throw new CompilationException(parameterVariableElement, CompilationErrorCode.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
               }
               if (parameterSimpleTypeMirror.getKind() != TypeKind.DECLARED)
               {
                  throw new CompilationException(parameterVariableElement, CompilationErrorCode.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
               }
               DeclaredType parameterSimpleType = (DeclaredType)parameterSimpleTypeMirror;
               if (!parameterSimpleType.asElement().toString().equals("java.lang.String"))
               {
                  throw new CompilationException(parameterVariableElement, CompilationErrorCode.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
               }
               parameterCardinalities.add(cardinality);
               parameterNames.add(parameterVariableElement.getSimpleName().toString());
            }

            //
            ElementHandle.Method origin = ElementHandle.Method.create(methodElt);

            // First remove the previous method
            methods.remove(origin);

            // Validate duplicate id within the same controller
            for (MethodMetaModel existing : methods.values())
            {
               if (existing.id != null && existing.id.equals(id))
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
               parameterCardinalities,
               parameterNames);
            methods.put(origin, method);
            modified = true;
            break;
         }
      }
   }
}
