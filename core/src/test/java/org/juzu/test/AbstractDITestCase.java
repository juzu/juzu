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
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.test.request.MockApplication;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractDITestCase extends AbstractTestCase
{

   /** . */
   private DIImplementation di;

   /** . */
   private String testName;

   /** . */
   private final TestListener listener = new TestListener() {
      public void addError(Test test, Throwable throwable) {
      }
      public void addFailure(Test test, AssertionFailedError assertionFailedError) {
      }
      public void endTest(Test test) {
         testName = null;
      }
      public void startTest(Test test) {
         testName = ((AbstractTestCase)test).getName();
      }
   };

   @Override
   public void run(TestResult result)
   {
      result.addListener(listener);

      //
      for (DIImplementation impl : DIImplementation.values())
      {
         di = impl;
         super.run(result);
      }

      //
      result.removeListener(listener);
   }

   public DIImplementation getDI()
   {
      return di;
   }

   public MockApplication<?> application(String... packageName)
   {
      CompilerHelper<File, File> helper = compiler(packageName);
      helper.assertCompile();
      InjectBootstrap bootstrap = di.bootstrap();
      return helper.application(bootstrap);
   }

}
