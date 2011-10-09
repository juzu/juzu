package org.juzu.impl.application;

import org.juzu.AmbiguousResolutionException;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Phase;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.utils.Builder;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerResolverTestCase extends AbstractTestCase
{

   public void testResolution() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "controller_resolver");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      Class<?> clazz = compiler.assertClass("controller_resolver.Controller_resolverApplication");
      ApplicationDescriptor desc = (ApplicationDescriptor)clazz.getField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);

      //
      ControllerMethod cm1 = resolver.resolve(Phase.RENDER, Collections.<String, String[]>emptyMap());
      assertNotNull(cm1);
      assertEquals("noArg", cm1.getMethodName());

      //
      ControllerMethod cm2 = resolver.resolve(Phase.RENDER, Collections.singletonMap("foo", new String[]{"bar"}));
      assertNotNull(cm2);
      assertEquals("fooArg", cm2.getMethodName());

      //
      ControllerMethod cm3 = resolver.resolve(Phase.RENDER, Collections.singletonMap("foo", new String[]{"foo_value"}));
      assertNotNull(cm3);
      assertEquals("fooBinding", cm3.getMethodName());

      //
      try
      {
         resolver.resolve(Phase.RENDER, Builder.map("foo", new String[]{"foo_value"}).put("bar", new String[]{"bar_value"}).build());
         fail();
      }
      catch (AmbiguousResolutionException ignore)
      {
      }
   }
}
