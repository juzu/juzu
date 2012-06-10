package juzu.impl.inject.spi.declared.bean.qualifier.declared;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.Color;
import juzu.impl.inject.spi.ColorizedLiteral;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierDeclaredBeanTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public DeclaredQualifierDeclaredBeanTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), Bean.Green.class);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.blue);
    assertNotNull(injected.red);
    assertNotNull(injected.green);
    assertNotSame(injected.blue.getId(), injected.red.getId());
    assertNotSame(injected.green.getId(), injected.red.getId());
    assertNotSame(injected.blue.getId(), injected.green.getId());
    assertInstanceOf(Bean.class, injected.blue);
    assertInstanceOf(Bean.class, injected.red);
    assertInstanceOf(Bean.Green.class, injected.green);
    assertNotInstanceOf(Bean.Green.class, injected.blue);
    assertNotInstanceOf(Bean.Green.class, injected.red);
  }
}
