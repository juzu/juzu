package juzu.impl.inject.spi.declared.bean.qualifier.declared;

import juzu.impl.utils.Tools;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bean {

  /** . */
  private final String id = Tools.nextUUID();

  public String getId() {
    return id;
  }

  public static class Green extends Bean {
  }
}
