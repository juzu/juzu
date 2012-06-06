package juzu.impl.spi.inject.declared.provider.qualifier.declared;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ColorlessProvider implements Provider<Bean> {

  public Bean get() {
    return new Bean();
  }
}
