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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Captures the precise state of an annotation in a serializable object. The content of the map are the value declared
 * by the annotation, however the state keeps also track of the default values and those can be queried using the
 * {@link #safeGet(String)} method.
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
  public Serializable safeGet(String key) {
    Serializable value = get(key);
    if (value == null && undeclared != null) {
      value = undeclared.get(key);
    }
    return value;
  }

  /**
   * Return true if the member is present and not declared.
   *
   * @param key the member key
   * @return true if the
   */
  public boolean isDeclared(String key) {
    return containsKey(key);
  }

  /**
   * Return true if the member is present and not declared.
   *
   * @param key the member key
   * @return true if the
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
        Serializable serialized = unwrap(value, xMember.getReturnType());
        target.put(key, serialized);
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
        ArrayList<Object> list = new ArrayList<Object>(array.size());
        for (Object element : array) {
          list.add(unwrap(element, componentType));
        }
        return list;
      }
      else {
        throw new UnsupportedOperationException("Impossible ? " + value + " " + value.getClass().getName());
      }
    }
    else if (value instanceof VariableElement) {
      return ((VariableElement)value).getSimpleName().toString();
    }
    else if (value instanceof DeclaredType) {
      return ElementHandle.Class.create((TypeElement)((DeclaredType)value).asElement());
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
