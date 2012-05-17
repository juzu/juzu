package org.juzu.impl.plugin;

import org.junit.Test;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.protocol.mock.MockApplication;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPluginTestCase extends AbstractInjectTestCase
{

   public PortletPluginTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testPortletClass() throws Exception
   {
      MockApplication<?> app = application("plugin", "portlet").init();
   }
}
