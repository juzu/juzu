/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TrimmingExceptionTestCase extends AbstractTestCase {

  @Test
  public void testEmpty() {
    try {
      TrimmingException.invoke(new TrimmingException.Callback() {
        public void call() throws Exception {
          throw new UnsupportedOperationException();
        }
      });
    }
    catch (TrimmingException e) {
      assertEquals(0, e.getStackTrace().length);
      assertEquals(null, e.getCause());
    }
  }

  @Test
  public void testException() {
    try {
      TrimmingException.invoke(new TrimmingException.Callback() {
        public void call() throws Exception {
          throw bar();
        }
      });
    }
    catch (TrimmingException e) {
      assertEquals(1, e.getStackTrace().length);
      StackTraceElement elt = e.getStackTrace()[0];
      assertEquals(getClass().getName(), elt.getClassName());
      assertEquals("bar", elt.getMethodName());
      assertEquals(null, e.getCause());
    }
  }

  @Test
  public void testCause() {
    try {
      TrimmingException.invoke(new TrimmingException.Callback() {
        public void call() throws Exception {
          throw foo();
        }
      });
    }
    catch (TrimmingException e) {
      assertEquals(1, e.getStackTrace().length);
      StackTraceElement elt = e.getStackTrace()[0];
      assertEquals(getClass().getName(), elt.getClassName());
      assertEquals("foo", elt.getMethodName());
      Throwable cause = e.getCause();
      assertEquals(2, cause.getStackTrace().length);
      elt = cause.getStackTrace()[0];
      assertEquals(getClass().getName(), elt.getClassName());
      assertEquals("bar", elt.getMethodName());
      elt = cause.getStackTrace()[1];
      assertEquals(getClass().getName(), elt.getClassName());
      assertEquals("foo", elt.getMethodName());
      assertEquals(null, cause.getCause());
    }
  }

  public Exception bar() {
    return new UnsupportedOperationException();
  }

  public Exception foo() {
    return new IllegalArgumentException(bar());
  }
}
