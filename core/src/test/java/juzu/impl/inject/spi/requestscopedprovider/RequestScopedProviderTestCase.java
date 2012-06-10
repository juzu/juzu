package juzu.impl.inject.spi.requestscopedprovider;

import juzu.Scope;
import juzu.impl.inject.spi.AbstractInjectManagerTestCase;
import juzu.impl.inject.spi.InjectImplementation;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestScopedProviderTestCase<B, I> extends AbstractInjectManagerTestCase<B, I> {

  public RequestScopedProviderTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareProvider(Bean.class, null, null, BeanProvider.class);
    boot(Scope.REQUEST);

    //
    beginScoping();
    try {
      Bean bean = getBean(Bean.class);
      assertNotNull(bean);
      assertNotNull(bean.provider);
    }
    finally {
      endScoping();
    }
  }
}
