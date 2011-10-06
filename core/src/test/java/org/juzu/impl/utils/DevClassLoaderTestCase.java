package org.juzu.impl.utils;

import junit.framework.TestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DevClassLoaderTestCase extends TestCase
{

   /** . */
   private File targetDir;

   @Override
   protected void setUp() throws Exception
   {
      String targetPath = System.getProperty("targetDir");
      assertNotNull(targetPath);
      File targetDir = new File(targetPath);
      assertTrue(targetDir.isDirectory());

      //
      this.targetDir = targetDir;
   }

   private ClassLoader getParentClassLoader()
   {
      ClassLoader systemCL = ClassLoader.getSystemClassLoader();
      ClassLoader extCL = systemCL.getParent();
      try
      {
         extCL.loadClass(Dev.class.getName());
         fail();
      }
      catch (ClassNotFoundException e)
      {
      }
      try
      {
         extCL.loadClass(Lib.class.getName());
         fail();
      }
      catch (ClassNotFoundException e)
      {
      }
      return extCL;
   }

   public void testExploded() throws Exception
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "exploded.war").addClass(Dev.class).addDirectory("WEB-INF/lib");
      File explodedDir = archive.as(ExplodedExporter.class).exportExploded(targetDir);
      File libJar = new File(explodedDir, "WEB-INF/lib/lib.jar");
      ShrinkWrap.create(JavaArchive.class).addClass(Lib.class).as(ZipExporter.class).exportZip(libJar);

      //
      File classesDir = new File(explodedDir, "WEB-INF/classes");
      assertTrue(classesDir.isDirectory());

      // Build a correct parent CL
      URLClassLoader cl = new URLClassLoader(new URL[]{classesDir.toURI().toURL(),libJar.toURI().toURL()}, getParentClassLoader());
      Class<?> devClass = cl.loadClass(Dev.class.getName());
      assertNotSame(devClass, Dev.class);
      Class<?> libClass = cl.loadClass(Lib.class.getName());
      assertNotSame(libClass, Lib.class);

      //
      DevClassLoader devCL = new DevClassLoader(cl);
      try
      {
         devCL.loadClass(Dev.class.getName());
         fail();
      }
      catch (ClassNotFoundException e)
      {
      }
      assertSame(libClass, devCL.loadClass(Lib.class.getName()));
   }
}
