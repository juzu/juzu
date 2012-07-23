package juzu.impl.controller;

import juzu.impl.application.ApplicationDescriptor;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.plugin.router.RouterDescriptor;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterTestCase extends AbstractInjectTestCase {

  public RouterTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testDeclaration() throws Exception {
    MockApplication<?> application = application("controller", "router", "declaration").init();

    //
    ApplicationDescriptor descriptor = application.getContext().getDescriptor();
    RouterDescriptor desc = (RouterDescriptor)descriptor.getPlugin("router");
    List<RouteDescriptor> routes = desc.getRoutes();
    assertEquals(3, routes.size());
  }
}
