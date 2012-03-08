/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;
import org.juzu.test.protocol.mock.MockApplication;

import java.io.File;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTestCase extends TestCase
{

   /**
    * Wait for at least one millisecond, based on the current time clock.
    *
    * @return the time captured after the wait
    */
   public static long waitForOneMillis()
   {
      long snapshot = System.currentTimeMillis();
      while (true)
      {
         try
         {
            long now = System.currentTimeMillis();
            if (snapshot < now)
            {
               return now;
            }
            else
            {
               snapshot = now;
               Thread.sleep(1);
            }
         }
         catch (InterruptedException e)
         {
            AssertionFailedError afe = new AssertionFailedError("Was not expecting interruption");
            afe.initCause(e);
            throw afe;
         }
      }
   }

   public static void fail(Throwable t)
   {
      throw failure(t);
   }

   public static AssertionFailedError failure(Throwable t)
   {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(t);
      return afe;
   }

   public static AssertionFailedError failure(String msg, Throwable t)
   {
      AssertionFailedError afe = new AssertionFailedError(msg);
      afe.initCause(t);
      return afe;
   }

   public static AssertionFailedError failure(String msg)
   {
      return new AssertionFailedError(msg);
   }
   
   public static <T> T assertInstanceOf(Class<T> expectedInstance, Object o)
   {
      if (expectedInstance.isInstance(o))
      {
         return expectedInstance.cast(o);
      }
      else
      {
         throw failure("Was expecting " + o + " to be an instance of " + expectedInstance.getName());
      }
   }

   public static <T> T assertNotInstanceOf(Class<?> expectedInstance, T o)
   {
      if (expectedInstance.isInstance(o))
      {
         throw failure("Was expecting " + o + " not to be an instance of " + expectedInstance.getName());
      }
      else
      {
         return o;
      }
   }

   public static void assertDelete(File f)
   {
      if (!f.exists())
      {
         throw failure("Was expecting file " + f.getAbsolutePath() + " to exist");
      }
      if (!f.delete())
      {
         throw failure("Was expecting file " + f.getAbsolutePath() + " to be deleted");
      }
   }

   public static DiskFileSystem diskFS(String... packageName)
   {
      File root = new File(System.getProperty("test.resources"));
      return new DiskFileSystem(root, packageName);
   }

   public CompilerHelper<File, File> compiler(String... packageName)
   {
      DiskFileSystem input = diskFS(packageName);

      if (packageName.length == 0)
      {
         throw failure("Cannot compile empty package");
      }

      //
      String outputPath = System.getProperty("test.generated.classes");
      File a = new File(outputPath);
      if (a.exists())
      {
         if (a.isFile())
         {
            throw failure("File " + outputPath + " already exist and is a file");
         }
      }
      else
      {
         if (!a.mkdirs())
         {
            throw failure("Could not create test generated source directory " + outputPath);
         }
      }

      // Find
      String pkg = Tools.join('.', packageName) + "#" + getName();
      File f2 = new File(a, pkg);
      for (int count = 0;;count++)
      {
         if (!f2.exists())
         {
            break;
         }
         else
         {
            f2 = new File(a, pkg + "-" + count);
         }
      }

      //
      if (!f2.mkdirs())
      {
         throw failure("Could not create test generated source directory " + f2.getAbsolutePath());
      }

      //
      File sourceOutputDir = new File(f2, "source-output");
      assertTrue(sourceOutputDir.mkdir());
      DiskFileSystem sourceOutput = new DiskFileSystem(sourceOutputDir);

      //
      File classOutputDir = new File(f2, "class-output");
      assertTrue(classOutputDir.mkdir());
      DiskFileSystem classOutput = new DiskFileSystem(classOutputDir);

      //
      File sourcePathDir = new File(f2, "source-path");
      assertTrue(sourcePathDir.mkdir());
      DiskFileSystem sourcePath = new DiskFileSystem(sourcePathDir);
      try
      {
         ReadFileSystem.copy(input, sourcePath);
      }
      catch (IOException e)
      {
         throw failure(e);
      }

      //
      return new CompilerHelper<File, File>(sourcePath, sourceOutput, classOutput);
   }

   public MockApplication<?> application(InjectImplementation injectImplementation, String... packageName)
   {
      CompilerHelper<File, File> helper = compiler(packageName);
      helper.assertCompile();
      return helper.application(injectImplementation);
   }

   public static void assertEquals(JSON expected, JSON test)
   {
      if (expected != null)
      {
         if (test == null)
         {
            throw failure("Was expected " + expected + " to be not null");
         }
         else if (!expected.equals(test))
         {
            StringBuilder sb = null;
            try
            {
               sb = new StringBuilder("expected <");
               expected.toString(sb, 2);
               sb.append(">  but was:<");
               test.toString(sb, 2);
               sb.append(">");
               throw failure(sb.toString());
            }
            catch (IOException e)
            {
               throw failure("Unexpected", e);
            }
         }
      }
      else
      {
         if (test != null)
         {
            throw failure("Was expected " + test + " to be null");
         }
      }
   }
}
