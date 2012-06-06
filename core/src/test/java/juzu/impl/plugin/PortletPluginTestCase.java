package juzu.impl.plugin;

import juzu.impl.spi.inject.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPluginTestCase extends AbstractInjectTestCase {

  public PortletPluginTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testPortletClass() throws Exception {
    MockApplication<?> app = application("plugin", "portlet").init();
  }
}
