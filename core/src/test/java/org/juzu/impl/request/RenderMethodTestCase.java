package org.juzu.impl.request;

import junit.framework.TestCase;
import org.juzu.application.PhaseLiteral;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.CompilerHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RenderMethodTestCase extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "controller1");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();
      compiler.assertClass("controller1.A");

      //
      a_Class = compiler.assertClass("controller1.A_");
   }

   /** . */
   private Class<?> a_Class;

   public void testNoArg() throws Exception
   {
      Field f = a_Class.getDeclaredField("noArg");
      PhaseLiteral l = (PhaseLiteral)f.get(null);

      //
      ControllerMethod cm = l.getDescriptor();
      assertEquals("noArg", cm.getMethodName());
      assertEquals(Collections.<ControllerParameter>emptyList(), cm.getArgumentParameters());
   }

   public void testStringArg() throws Exception
   {
      Field f = a_Class.getDeclaredField("oneArg");
      PhaseLiteral l = (PhaseLiteral)f.get(null);

      //
      ControllerMethod cm = l.getDescriptor();
      assertEquals("oneArg", cm.getMethodName());
      assertEquals(Arrays.asList(new ControllerParameter("foo")), cm.getArgumentParameters());
   }
}
