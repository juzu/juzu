package org.juzu.impl.spi.inject.configuration;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.spi.inject.spring.SpringBuilder;
import org.juzu.impl.utils.Tools;

import java.io.InputStream;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConfigurationTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public ConfigurationTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testURL() throws Exception
   {
      if (di == InjectImplementation.INJECT_SPRING)
      {
         URL configurationURL = Bean.class.getResource("spring.xml");
         assertNotNull(configurationURL);
         InputStream in = configurationURL.openStream();
         assertNotNull(in);
         Tools.safeClose(in);

         //
         init();
         bootstrap.declareBean(Injected.class, null, null, null);
         ((SpringBuilder)bootstrap).setConfigurationURL(configurationURL);
         boot();

         //
         Injected injected = getBean(Injected.class);
         assertNotNull(injected);
         assertNotNull(injected.getDeclared());

         //
         Bean declared = getBean(Bean.class);
         assertNotNull(declared);
         declared = (Bean)getBean("declared");
         assertNotNull(declared);
      }
   }
}
