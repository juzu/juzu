package juzu.impl.spi.inject.cdi;

import juzu.Scope;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractDeclaredBean extends AbstractBean {

  AbstractDeclaredBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers) {
    super(type, scope, qualifiers);
  }
}
