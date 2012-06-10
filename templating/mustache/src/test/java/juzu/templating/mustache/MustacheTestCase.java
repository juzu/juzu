package juzu.templating.mustache;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockRenderBridge;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MustacheTestCase extends AbstractInjectTestCase {

  public MustacheTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testBasic() throws Exception {
    MockApplication<?> app = application("basic").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("foo", render.assertStringResult());
  }

  @Test
  public void testResolveParameter() throws Exception {
    MockApplication<?> app = application("resolveparameter").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("bar", render.assertStringResult());
  }

  @Test
  public void testResolveBean() throws Exception {
    MockApplication<?> app = application("resolvebean").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("bar", render.assertStringResult());
  }

  @Test
  public void testPartial() throws Exception {
    MockApplication<?> app = application("partial").init();
    MockClient client = app.client();
    MockRenderBridge render = client.render();
    assertEquals("<bar>foo</bar>", render.assertStringResult());
  }

  @Test
  public void testParameterDeclaration() throws Exception {
    if (getDI() != InjectImplementation.INJECT_GUICE) {
      MockApplication<?> app = application("parameterdeclaration").init();
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals("bar", render.assertStringResult());
    }
  }
}
