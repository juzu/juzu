package juzu.impl.inject.spi.guice;

import com.google.inject.Binding;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class GuiceBean {

  final Binding<?> binding;

  public GuiceBean(Binding<?> binding) {
    this.binding = binding;
  }
}
