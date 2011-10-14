package org.juzu.impl.application;

import org.juzu.AmbiguousResolutionException;
import org.juzu.application.ApplicationDescriptor;
import org.juzu.application.Phase;
import org.juzu.impl.request.ControllerMethod;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.AbstractTestCase;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerTestCase extends AbstractTestCase
{

   public void testDefaultController() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "default_controller");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.default_controller.Default_controllerApplication");
      Class<?> aClass = compiler.assertClass("application.default_controller.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      assertSame(aClass, desc.getDefaultController());
   }

   public void testResolverDefaultMethod() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "default_method");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_method.Default_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, Collections.<String, String[]>emptyMap());
      assertEquals("index", method.getName());
   }

   public void testResolverAmbiguousMethod() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "ambiguous_method");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.ambiguous_method.Ambiguous_methodApplication");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      try
      {
         resolver.resolve(Phase.RENDER, Collections.<String, String[]>emptyMap());
         fail();
      }
      catch (AmbiguousResolutionException e)
      {
      }
   }

   public void testResolverDefaultController() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "application", "resolver", "default_controller");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      Class<?> appClass = compiler.assertClass("application.resolver.default_controller.Default_controllerApplication");
      Class<?> aClass = compiler.assertClass("application.resolver.default_controller.A");

      //
      ApplicationDescriptor desc = (ApplicationDescriptor)appClass.getDeclaredField("DESCRIPTOR").get(null);
      ControllerResolver resolver = new ControllerResolver(desc);
      ControllerMethod method = resolver.resolve(Phase.RENDER, Collections.<String, String[]>emptyMap());
      assertEquals("index", method.getName());
      assertSame(aClass, method.getMethod().getDeclaringClass());
   }
}
