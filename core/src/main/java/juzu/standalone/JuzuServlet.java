package juzu.standalone;

import juzu.PropertyType;
import juzu.impl.bridge.spi.standalone.ServletBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuServlet extends ServletBridge {

  /** . */
  public static final class PATH extends PropertyType<String> {}

  /** . */
  public static final PATH PATH = new PATH();

  /** . */
  public static final class REDIRECT_AFTER_ACTION extends PropertyType<Boolean> {}

  /** . */
  public static final REDIRECT_AFTER_ACTION REDIRECT_AFTER_ACTION = new REDIRECT_AFTER_ACTION();

}
