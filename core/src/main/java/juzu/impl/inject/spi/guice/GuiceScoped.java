package juzu.impl.inject.spi.guice;

import juzu.impl.inject.Scoped;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class GuiceScoped implements Scoped {

  /** . */
  final Object o;

  GuiceScoped(Object o) {
    this.o = o;
  }

  public Object get() {
    return o;
  }

  public void destroy() {
    GuiceManager.invokePreDestroy(o);
  }
}
