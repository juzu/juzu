package juzu.impl.spi.inject.cdi;

import juzu.Scope;

import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractSingletonBean extends AbstractBean {

  protected AbstractSingletonBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers) {
    super(type, scope, qualifiers);
  }

  public void destroy(Object instance, CreationalContext ctx) {
    ctx.release();
  }
}
