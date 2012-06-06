package juzu.impl.compiler;

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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AnnotationData extends HashMap<String, Serializable> {

  public static AnnotationData create(AnnotationMirror annotation) throws NullPointerException {
    if (annotation == null) {
      throw new NullPointerException("No null annotation allowed");
    }

    //
    AnnotationData values = new AnnotationData();
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
