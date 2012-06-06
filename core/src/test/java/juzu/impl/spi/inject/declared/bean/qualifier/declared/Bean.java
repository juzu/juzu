package juzu.impl.spi.inject.declared.bean.qualifier.declared;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bean {

  /** . */
  private final String id = "" + Math.random();

  public String getId() {
    return id;
  }

  public static class Green extends Bean {
  }
}
