package juzu.plugin.portlet;

import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPluginTestCase extends AbstractInjectTestCase {

  public PortletPluginTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testClass() throws Exception {
    MockApplication<?> app = application("plugin", "portlet", "base").init();
    Class<?> portletClass = app.getContext().getClassLoader().loadClass("plugin.portlet.base.BasePortlet");
  }

  @Test
  public void testName() throws Exception {
    MockApplication<?> app = application("plugin", "portlet", "name").init();
    Class<?> portletClass = app.getContext().getClassLoader().loadClass("plugin.portlet.name.Foo");
  }
}
