package juzu.impl.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Uniquely identifies a method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class MethodHandle implements Iterable<String> {

  public static MethodHandle parse(String s) throws NullPointerException, IllegalArgumentException {
    int method = s.indexOf('#');
    if (method == -1) {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }
    int leftParenthesis = s.indexOf('(', method + 1);
    if (leftParenthesis == -1 || leftParenthesis > s.length() - 2) {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }
    if (s.charAt(s.length() - 1) != ')') {
      throw new IllegalArgumentException("Invalid method handle " + s);
    }

    //
    String type = s.substring(0, method);
    String name = s.substring(method + 1, leftParenthesis);

    //
    if (s.length() - leftParenthesis == 2) {
      return new MethodHandle(type, name);
    } else {
      String[] list = EMPTY_STRINGS;
      for (String parameter : Spliterator.split(s, leftParenthesis + 1, s.length() - 1, ',', new ArrayList<String>())) {
        if (parameter.length() == 0) {
          throw new IllegalArgumentException();
        }
        list = Tools.appendTo(list, parameter);
      }
      return new MethodHandle(type, name, list);
    }
  }

  /** . */
  private static final String[] EMPTY_STRINGS = new String[0];

  /** . */
  private final String type;

  /** . */
  private final String name;

  /** . */
  private final String[] parameters;

  /** . */
  private String toString;

  public MethodHandle(Method method) throws NullPointerException {
    if (method == null) {
      throw new NullPointerException("No null method accepted");
    }

    Class<?>[] parameterTypes = method.getParameterTypes();
    String[] parameters = new String[parameterTypes.length];
    for (int i = 0;i < parameters.length;i++) {
      parameters[i] = parameterTypes[i].getName();
    }

    //
    this.type = method.getDeclaringClass().getName();
    this.name = method.getName();
    this.parameters = parameters.clone();
    this.toString = null;
  }

  public MethodHandle(String type, String name) {
    this.type = type;
    this.name = name;
    this.parameters = EMPTY_STRINGS;
  }

  public MethodHandle(String type, String name, String... parameters) {
    this.type = type;
    this.name = name;
    this.parameters = parameters.length == 0 ? parameters : parameters.clone();
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getParameterSize() {
    return parameters.length;
  }

  public String getParameterAt(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index > parameters.length) {
      throw new IndexOutOfBoundsException("Bad index " + index);
    }
    return parameters[index];
  }

  public Iterator<String> iterator() {
    return Tools.iterator(parameters);
  }

  @Override
  public int hashCode() {
    return type.hashCode() ^ name.hashCode() ^ Arrays.hashCode(parameters);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof MethodHandle) {
      MethodHandle that = (MethodHandle)obj;
      return type.equals(that.type) && name.equals(that.name) && Arrays.equals(parameters, that.parameters);
    }
    return false;
  }

  @Override
  public String toString() {
    if (toString == null) {
      toString = Tools.join(new StringBuilder().append(type).append('#').append(name).append('('), ',', parameters).append(')').toString();
    }
    return toString;
  }
}
