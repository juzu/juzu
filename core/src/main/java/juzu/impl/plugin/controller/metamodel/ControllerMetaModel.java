/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.controller.metamodel;

import juzu.Mapped;
import juzu.Param;
import juzu.impl.compiler.ProcessingContext;
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
import java.util.logging.Level;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModel extends MetaModelObject implements Iterable<HandlerMetaModel> {

  /** . */
  public static final MessageCode CANNOT_WRITE_CONTROLLER_COMPANION = new MessageCode("CANNOT_WRITE_CONTROLLER_COMPANION", "The controller companion %1$s cannot be written");

  /** . */
  public static final MessageCode CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED = new MessageCode("CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED", "The method parameter type %1s should be a string or annotated with @juzu.Param");

  /** . */
  public static final MessageCode CONTROLLER_IS_ABSTRACT = new MessageCode("CONTROLLER_IS_ABSTRACT", "The controller class %1s cannot be abstract");

  /** A flag for handling modified event. */
  boolean modified;

  /** The application. */
  ControllersMetaModel controllers;

  /** . */
  final ElementHandle.Type handle;

  public ControllerMetaModel(ElementHandle.Type handle) {
    this.handle = handle;
    this.modified = false;
  }

  public Iterator<HandlerMetaModel> iterator() {
    return getHandlers().iterator();
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("handle", handle);
    json.map("methods", getHandlers());
    return json;
  }

  public ControllersMetaModel getControllers() {
    return controllers;
  }

  public ElementHandle.Type getHandle() {
    return handle;
  }

  public Collection<HandlerMetaModel> getHandlers() {
    return getChildren(HandlerMetaModel.class);
  }

  private PhaseParameterMetaModel foo(
      VariableElement parameterVariableElt,
      String parameterName,
      Cardinality parameterCardinality,
      String type,
      String valueType) {
    // Not sure we should use @Param for this (i.e for now it looks hackish)
    // however it does make sense later to use the regex part for non router
    // parameters
    Param param = parameterVariableElt.getAnnotation(Param.class);
    String alias = param != null && param.name().length() > 0 ? param.name() : null;
    return new PhaseParameterMetaModel(parameterName, parameterCardinality, type, valueType, alias);
  }

  private ParameterMetaModel foo(ModuleMetaModel context, VariableElement parameterVariableElt, TypeMirror parameterTypeMirror) {
    String type = context.processingContext.getLiteralName(parameterTypeMirror);

    //
    String parameterName = parameterVariableElt.getSimpleName().toString();

    //
    if (parameterVariableElt.getAnnotation(Mapped.class) != null) {
      return new BeanParameterMetaModel(parameterName, type);
    } else {

      // Determine cardinality
      TypeMirror parameterValueTypeMirror;
      Cardinality parameterCardinality;
      switch (parameterTypeMirror.getKind()) {
        case INT:
          return foo(parameterVariableElt, parameterName, Cardinality.SINGLE, "int", "int");
        case DECLARED:
          DeclaredType dt = (DeclaredType)parameterTypeMirror;
          TypeElement col = context.processingContext.getTypeElement("java.util.List");
          TypeMirror tm = context.processingContext.erasure(col.asType());
          TypeMirror err = context.processingContext.erasure(dt);
          if (err.equals(tm)) {
            if (dt.getTypeArguments().size() != 1) {
              throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
            } else {
              parameterCardinality = Cardinality.LIST;
              parameterValueTypeMirror = dt.getTypeArguments().get(0);
              if (parameterValueTypeMirror.getKind() != TypeKind.DECLARED) {
                throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
              }
            }
          } else {
            parameterCardinality = Cardinality.SINGLE;
            parameterValueTypeMirror = parameterTypeMirror;
          }
          break;
        case ARRAY:
          // Unwrap array
          ArrayType arrayType = (ArrayType)parameterTypeMirror;
          parameterCardinality = Cardinality.ARRAY;
          parameterValueTypeMirror = arrayType.getComponentType();
          switch (parameterValueTypeMirror.getKind()) {
            case DECLARED:
              break;
            case INT:
              return foo(parameterVariableElt, parameterName, Cardinality.ARRAY, "int[]", "int");
            default:
              throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
          }
          break;
        default:
          throw CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(parameterVariableElt);
      }

      //
      TypeElement valueType = (TypeElement)context.processingContext.asElement(parameterValueTypeMirror);
      ElementHandle.Type valueTypeHandle = ElementHandle.Type.create(valueType);

      //
      if (valueType.toString().equals("java.lang.String") || controllers.plugin.valueTypes.contains(valueTypeHandle)) {
        return foo(parameterVariableElt, parameterName, parameterCardinality, type, valueType.toString());
      } else {
        return new ContextualParameterMetaModel(parameterName, type);
      }
    }
  }

  void addMethod(ModuleMetaModel context, AnnotationKey annotationKey, AnnotationState annotationState) {

    //
    String id = (String)annotationState.get("id");
    ElementHandle.Method methodHandle = (ElementHandle.Method)annotationKey.getElement();
    ExecutableElement methodElt = context.processingContext.get(methodHandle);
    ProcessingContext.log.log(Level.FINE, "Adding method " + methodElt + " to controller class " + handle);

    //
    for (Phase phase : Phase.values()) {
      if (phase.annotation.getName().equals(annotationKey.getType().toString())) {

        // First remove the previous method
        Key<HandlerMetaModel> key = Key.of(methodHandle, HandlerMetaModel.class);
        if (getChild(key) == null) {
          // Parameters
          ArrayList<ParameterMetaModel> parameters = new ArrayList<ParameterMetaModel>();
          List<? extends TypeMirror> parameterTypeMirrors = ((ExecutableType)methodElt.asType()).getParameterTypes();
          List<? extends VariableElement> parameterVariableElements = methodElt.getParameters();
          for (int i = 0;i < parameterTypeMirrors.size();i++) {
            VariableElement parameterVariableElt = parameterVariableElements.get(i);
            TypeMirror parameterTypeMirror = parameterTypeMirrors.get(i);
            ParameterMetaModel parameterMetaModel = foo(context, parameterVariableElt, parameterTypeMirror);
            parameters.add(parameterMetaModel);
          }

          //
          HandlerMetaModel method = new HandlerMetaModel(
            methodHandle,
            id,
            phase,
            methodElt.getSimpleName().toString(),
            parameters);
          addChild(key, method);
          modified = true;
          ProcessingContext.log.log(Level.FINE, "Added method " + methodHandle + " to controller class " + handle);
        }
        break;
      }
    }
  }

  void removeMethod(ElementHandle.Method handle) {
    ProcessingContext.log.log(Level.FINE, "Removing method " + handle + " from controller class " + handle);
    if (removeChild(Key.of(handle, HandlerMetaModel.class)) != null) {
      modified = true;
      ProcessingContext.log.log(Level.FINE, "Removed method " + handle + " from controller class " + handle);
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
