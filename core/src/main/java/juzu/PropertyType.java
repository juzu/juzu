package juzu;

import juzu.asset.Asset;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PropertyType<T> {

  /** Script type literal. */
  public static class SCRIPT extends PropertyType<Asset> {}

  /** Script type literal instance. */
  public static SCRIPT SCRIPT = new SCRIPT();

  /** Stylesheet type literal. */
  public static class STYLESHEET extends PropertyType<Asset> {}

  /** Stylesheet literal instance. */
  public static STYLESHEET STYLESHEET = new STYLESHEET();

  /** Stylesheet type literal. */
  public static class TITLE extends PropertyType<String> {}

  /** Stylesheet literal instance. */
  public static TITLE TITLE = new TITLE();

  /** . */
  public static final class PATH extends PropertyType<String> {}

  /** . */
  public static final PropertyType.PATH PATH = new PropertyType.PATH();

  /** . */
  public static final class REDIRECT_AFTER_ACTION extends PropertyType<Boolean> {}

  /** . */
  public static final REDIRECT_AFTER_ACTION REDIRECT_AFTER_ACTION = new REDIRECT_AFTER_ACTION();

  /** Header type literal. */
  public static class HEADER extends PropertyType<Map.Entry<String, String[]>> {}

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
