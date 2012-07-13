package juzu.standalone;

import juzu.PropertyType;
import juzu.impl.bridge.spi.standalone.ServletBridge;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JuzuServlet extends ServletBridge {

  /** . */
  public static final class PATH extends PropertyType<String> {}

  /** . */
  public static final PATH PATH = new PATH();
}
