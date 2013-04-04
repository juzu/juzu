/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
