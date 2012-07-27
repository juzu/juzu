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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the state of an annotation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AnnotationState extends HashMap<String, Serializable> {

  public static AnnotationState create(AnnotationMirror annotation) throws NullPointerException {
    if (annotation == null) {
      throw new NullPointerException("No null annotation allowed");
    }

    //
    AnnotationState values = new AnnotationState();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
      String m = entry.getKey().getSimpleName().toString();
      Serializable value = unwrap(entry.getValue(), entry.getKey().getReturnType());
      values.put(m, value);
    }
    return values;
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
