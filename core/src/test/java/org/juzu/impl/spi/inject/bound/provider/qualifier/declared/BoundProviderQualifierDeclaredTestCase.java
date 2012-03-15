package org.juzu.impl.spi.inject.bound.provider.qualifier.declared;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.Color;
import org.juzu.impl.spi.inject.ColorizedLiteral;
import org.juzu.impl.spi.inject.InjectImplementation;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundProviderQualifierDeclaredTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public BoundProviderQualifierDeclaredTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      init();
      bootstrap.declareBean(Injected.class, null, null, null);
      Bean blue = new Bean();
      Bean red = new Bean();
      Bean green = new Bean.Green();
      bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), new BeanProvider(blue));
      bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), new BeanProvider(red));
      bootstrap.bindProvider(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), new BeanProvider(green));
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertSame(blue, injected.blue);
      assertSame(red, injected.red);
      assertSame(green, injected.green);
   }
}
