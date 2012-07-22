package juzu;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PropertyType<T> {

  /** Header type literal. */
  public static class HEADER extends PropertyType<Map.Entry<String, String[]>> {
  }

  /** Header literal instance. */
  public static HEADER HEADER = new HEADER();

  protected PropertyType() throws NullPointerException {
  }

  public final T cast(Object o) {
    return (T)o;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this || obj != null && getClass().equals(obj.getClass());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
