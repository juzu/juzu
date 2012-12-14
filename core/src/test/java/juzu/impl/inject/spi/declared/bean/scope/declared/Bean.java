package juzu.impl.inject.spi.declared.bean.scope.declared;

import juzu.impl.utils.Tools;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bean {

  /** . */
  private String value;

  public Bean() {
    this.value = Tools.nextUUID();
  }

  public String getValue() {
    return value;
  }
}
