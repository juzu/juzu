package juzu.impl.inject.spi.declared.provider.qualifier.declared;

import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.Color;
import juzu.impl.inject.spi.ColorizedLiteral;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierDeclaredProviderTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public DeclaredQualifierDeclaredProviderTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), ColorlessProvider.class);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), ColorlessProvider.class);
    bootstrap.declareProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), GreenProvider.class);
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
