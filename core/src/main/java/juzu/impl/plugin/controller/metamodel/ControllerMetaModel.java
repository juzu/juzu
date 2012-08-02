/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.controller.metamodel;

import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.Cardinality;
import juzu.impl.common.JSON;
import juzu.request.Phase;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModel extends MetaModelObject implements Iterable<MethodMetaModel> {

  /** . */
  public static final MessageCode CANNOT_WRITE_CONTROLLER_COMPANION = new MessageCode("CANNOT_WRITE_CONTROLLER_COMPANION", "The controller companion %1$s cannot be written");

  /** . */
  public static final MessageCode CONTROLLER_METHOD_NOT_RESOLVED = new MessageCode("CONTROLLER_METHOD_NOT_RESOLVED", "The controller method cannot be resolved %1$s");

  /** . */
  public static final MessageCode CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED = new MessageCode("CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED", "The method parameter type %1s should be a string or annotated with @juzu.Param");

  /** A flag for handling modified event. */
  boolean modified;

  /** The application. */
  ControllersMetaModel controllers;

  /** . */
  final ElementHandle.Class handle;

  public ControllerMetaModel(ElementHandle.Class handle) {
    this.handle = handle;
    this.modified = false;
  }

  public Iterator<MethodMetaModel> iterator() {
    return getMethods().iterator();
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("handle", handle);
    json.map("methods", getMethods());
    return json;
  }

  public ControllersMetaModel getControllers() {
    return controllers;
  }

  public ElementHandle.Class getHandle() {
    return handle;
  }

  public Collection<MethodMetaModel> getMethods() {
    return getChildren(MethodMetaModel.class);
  }

  public void remove(ElementHandle.Method handle) {
    removeChild(Key.of(handle, MethodMetaModel.class));
  }

  void addMethod(
    ModuleMetaModel context,
    AnnotationKey key2,
    AnnotationState annotation) {

    //
    String id = (String)annotation.get("id");

    ExecutableElement methodElt = (ExecutableElement)context.env.get(key2.getElement());

    //
    for (Phase phase : Phase.values()) {
      if (phase.annotation.getName().equals(key2.getType().toString())) {
        ElementHandle.Method origin = (ElementHandle.Method)key2.getElement();

        // First remove the previous method
        Key<MethodMetaModel> key = Key.of(origin, MethodMetaModel.class);
        if (getChild(key) == null) {
          // Parameters
          ArrayList<ParameterMetaModel> parameters = new ArrayList<ParameterMetaModel>();
          List<? extends TypeMirror> parameterTypeMirrors = ((ExecutableType)methodElt.asType()).getParameterTypes();
          List<? extends VariableElement> parameterVariableElements = methodElt.getParameters();
          for (int i = 0;i < parameterTypeMirrors.size();i++) {
            VariableElement parameterVariableElt = parameterVariableElements.get(i);
            TypeMirror parameterTypeMirror = parameterTypeMirrors.get(i);
            TypeMirror erasedParameterTypeMirror = context.env.erasure(parameterTypeMirror);
            String parameterType = erasedParameterTypeMirror.toString();
            //
            String parameterName = parameterVariableElt.getSimpleName().toString();

            // Determine cardinality
            TypeMirror parameterSimpleTypeMirror;
            Cardinality parameterCardinality;
            switch (parameterTypeMirror.getKind()) {
              case DECLARED:
                DeclaredType dt = (DeclaredType)parameterTypeMirror;
                TypeElement col = context.env.getTypeElement("java.util.List");
                TypeMirror tm = context.env.erasure(col.asType());
                TypeMirror err = context.env.erasure(dt);
                // context.env.isSubtype(err, tm)
                if (err.equals(tm)) {
                  if (dt.getTypeArguments().size() != 1) {
                    throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
                  }
                  else {
                    parameterCardinality = Cardinality.LIST;
                    parameterSimpleTypeMirror = dt.getTypeArguments().get(0);
                  }
                }
                else {
                  parameterCardinality = Cardinality.SINGLE;
                  parameterSimpleTypeMirror = parameterTypeMirror;
                }
                break;
              case ARRAY:
                // Unwrap array
                ArrayType arrayType = (ArrayType)parameterTypeMirror;
                parameterCardinality = Cardinality.ARRAY;
                parameterSimpleTypeMirror = arrayType.getComponentType();
                break;
              default:
                throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
            }
            if (parameterSimpleTypeMirror.getKind() != TypeKind.DECLARED) {
              throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
            }

            //
            TypeElement te = (TypeElement)context.env.asElement(parameterSimpleTypeMirror);
            ElementHandle.Class a = ElementHandle.Class.create(te);

            //
            parameters.add(new ParameterMetaModel(parameterName, parameterCardinality, a, parameterType));
          }

          //
          MethodMetaModel method = new MethodMetaModel(
            origin,
            id,
            phase,
            methodElt.getSimpleName().toString(),
            parameters);
          addChild(key, method);
          modified = true;
        }
        break;
      }
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ControllersMetaModel) {
      queue(MetaModelEvent.createRemoved(this));
      controllers = null;
    }
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ControllersMetaModel) {
      controllers = (ControllersMetaModel)parent;
      queue(MetaModelEvent.createAdded(this));
    }
  }
}
