package org.juzu.impl.spi.inject.bound.bean.qualifier.declared;

import org.junit.Test;
import org.juzu.impl.spi.inject.AbstractInjectManagerTestCase;
import org.juzu.impl.spi.inject.Color;
import org.juzu.impl.spi.inject.ColorizedLiteral;
import org.juzu.impl.spi.inject.InjectImplementation;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BoundBeanQualifierDeclaredTestCase<B, I> extends AbstractInjectManagerTestCase<B, I>
{

   public BoundBeanQualifierDeclaredTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void test() throws Exception
   {
      Bean blue = new Bean();
      Bean red = new Bean();
      init();
      bootstrap.declareBean(Injected.class, null, null);
      bootstrap.bindBean(Bean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), blue);
      bootstrap.bindBean(Bean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), red);
      boot();

      //
      Injected injected = getBean(Injected.class);
      assertNotNull(injected);
      assertNotNull(injected.blue);
      assertNotNull(injected.red);
      assertSame(blue, injected.blue);
      assertSame(red, injected.red);
   }
}

