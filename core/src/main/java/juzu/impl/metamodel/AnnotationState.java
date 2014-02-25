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

package juzu.impl.metamodel;

import juzu.impl.compiler.ElementHandle;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the precise state of an annotation in a serializable object. The content of the map are the value declared
 * by the annotation, however the state keeps also track of the default values and those can be queried using the
 * {@link #resolve(String)} method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AnnotationState extends HashMap<String, Serializable> {

  /** Set indicating which member were really declared in the annotation (i.e the default values). */
  private HashMap<String, Serializable> undeclared;

  /**
   * Returns the annotation member if present, otherwise the default value.
   *
   * @param key the member key
   * @return the serializable value
   */
  public Serializable resolve(String key) {
    Serializable value = get(key);
    if (value == null && undeclared != null) {
      value = undeclared.get(key);
    }
    return value;
  }

  /**
   * Return true if the member is present.
   *
   * @param key the member key
   * @return true if the annotation declared the specified member
   */
  public boolean isDeclared(String key) {
    return containsKey(key);
  }

  /**
   * Return true if the member is not declared.
   *
   * @param key the member key
   * @return true if the annotation does not declare the specified member
   */
  public boolean isUndeclared(String key) {
    return undeclared == null || undeclared.containsKey(key);
  }

  /**
   * Get an annotation state from the specified element and annotation type element, or null when
   * it cannot be found.
   *
   * @param element the element
   * @param annotationType the annotation type
   * @return the annotation state
   * @throws NullPointerException
   */
  public static AnnotationState get(Element element, TypeMirror annotationType) throws NullPointerException {
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      if (annotation.getAnnotationType().equals(annotationType)) {
        return AnnotationState.create(annotation);
      }
    }
    return null;
  }

  public static AnnotationState create(AnnotationMirror annotation) throws NullPointerException {
    if (annotation == null) {
      throw new NullPointerException("No null annotation allowed");
    }

    //
    AnnotationState state = new AnnotationState();

    //
    TypeElement annotationTypeElement = (TypeElement)annotation.getAnnotationType().asElement();
    Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
    for (Element member : annotationTypeElement.getEnclosedElements()) {
      if (member instanceof ExecutableElement) {
        ExecutableElement xMember = (ExecutableElement)member;
        AnnotationValue value = values.get(xMember);
        String key = xMember.getSimpleName().toString();
        HashMap<String, Serializable> target;
        if (value == null) {
          if (state.undeclared == null) {
            state.undeclared = new HashMap<String, Serializable>();
          }
          target = state.undeclared;
          value = xMember.getDefaultValue();
        } else {
          target = state;
        }
        if (value != null) {
          Serializable serialized = unwrap(value, xMember.getReturnType());
          target.put(key, serialized);
        }
      }
    }

    //
    return state;
  }

  private static Serializable unwrap(Object value, TypeMirror type) {
    if (value instanceof AnnotationValue) {
      value = ((AnnotationValue)value).getValue();
    }

    //
    if (type instanceof ArrayType) {
      TypeMirror componentType = ((ArrayType)type).getComponentType();
      if (value instanceof List) {
        List<?> array = (List<?>)value;
        if (array.size() == 0) {
          // Need to force the cast, javadoc says it is serializable
          return (Serializable)Collections.<Serializable>emptyList();
        } else {
          ArrayList<Object> list = new ArrayList<Object>(array.size());
          for (Object element : array) {
            list.add(unwrap(element, componentType));
          }
          return list;
        }
      }
      else {
        throw new UnsupportedOperationException("Impossible ? " + value + " " + value.getClass().getName());
      }
    }
    else if (value instanceof VariableElement) {
      return ((VariableElement)value).getSimpleName().toString();
    }
    else if (value instanceof DeclaredType) {
      return ElementHandle.Type.create((TypeElement)((DeclaredType)value).asElement());
    }
    else if (value instanceof AnnotationMirror) {
      return create((AnnotationMirror)value);
    }
    else if (value instanceof Serializable) {
      return (Serializable)value;
    }
    else {
      throw new UnsupportedOperationException("Need to unwrap not serializable type " + value + " " +
        value.getClass().getName());
    }
  }
}
