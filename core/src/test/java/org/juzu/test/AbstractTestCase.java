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
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.utils.Tools;
import org.juzu.test.request.MockApplication;

import java.io.File;

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

   public static AssertionFailedError failure(String msg)
   {
      return new AssertionFailedError(msg);
   }

   public static DiskFileSystem diskFS(String... packageName)
   {
      File root = new File(System.getProperty("test.resources"));
      return new DiskFileSystem(root, packageName);
   }

   public static CompilerHelper<File, File> compiler(String... packageName)
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
      File f2;
      String pkg = Tools.join('.', packageName);
      f2 = new File(a, pkg);
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
      DiskFileSystem output = new DiskFileSystem(f2);

      //
      return new CompilerHelper<File, File>(input, output);
   }

   public static MockApplication<?> application(String... packageName)
   {
      CompilerHelper<File, File> helper = compiler(packageName);
      helper.assertCompile();
      return helper.application();
   }
}
