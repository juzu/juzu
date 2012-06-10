package juzu.impl.inject.spi.cdi;

import juzu.Scope;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredBean extends AbstractDeclaredBean {

  /** . */
  protected InjectionTarget it;

  /** . */
  protected AnnotatedType at;

  DeclaredBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers) {
    super(type, scope, qualifiers);
  }

  void register(BeanManager manager) {
    super.register(manager);

    //
    AnnotatedType at = manager.createAnnotatedType(type);
    InjectionTarget it = manager.createInjectionTarget(at);

    //
    this.it = it;
    this.at = at;
  }

  public Object create(CreationalContext ctx) {
    Object instance = it.produce(ctx);
    it.inject(instance, ctx);
    it.postConstruct(instance);
    return instance;
  }


  public void destroy(Object instance, CreationalContext ctx) {
    it.preDestroy(instance);
    it.dispose(instance);
    ctx.release();
  }
}
