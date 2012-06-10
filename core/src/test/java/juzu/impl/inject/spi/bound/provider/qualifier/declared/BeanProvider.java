package juzu.impl.inject.spi.bound.provider.qualifier.declared;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BeanProvider implements Provider<Bean> {

  /** . */
  private final Bean product;

  public BeanProvider(Bean product) {
    this.product = product;
  }

  public Bean get() {
    return product;
  }
}
