package org.juzu.impl.application;

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
      Class<?> aClass = compiler.assertClass("controller_resolver.A");
      Class<?> clazz = compiler.assertClass("controller_resolver.Controller_resolverApplication");
      ApplicationDescriptor desc = (ApplicationDescriptor)clazz.getField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod cm1_ = desc.getControllerMethod(aClass, "noArg");
      ControllerMethod cm2_ = desc.getControllerMethod(aClass, "fooArg", String.class);

      //
      ControllerMethod cm1 = resolver.resolve(Phase.RENDER, Collections.<String, String[]>singletonMap("op", new String[]{cm1_.getId()}));
      assertNotNull(cm1);
      assertEquals("noArg", cm1.getName());

      //
      ControllerMethod cm2 = resolver.resolve(Phase.RENDER, Collections.<String, String[]>singletonMap("op", new String[]{cm2_.getId()}));
      assertNotNull(cm2);
      assertEquals("fooArg", cm2.getName());

      //
//      try
//      {
//         resolver.resolve(Phase.RENDER, Builder.map("foo", new String[]{"foo_value"}).put("bar", new String[]{"bar_value"}).build());
//         fail();
//      }
//      catch (AmbiguousResolutionException ignore)
//      {
//      }
   }
}
