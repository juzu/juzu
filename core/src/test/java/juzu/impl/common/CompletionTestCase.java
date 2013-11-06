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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Julien Viet */
public class CompletionTestCase extends AbstractTestCase {

  @Test
  public void testFutureComplete() {
    final AtomicInteger count = new AtomicInteger();
    Completion<String> completion = Completion.future(new Callable<String>() {
      public String call() throws Exception {
        count.incrementAndGet();
        return "foo";
      }
    });
    assertEquals(0, count.get());
    assertEquals("foo", completion.get());
    assertEquals(1, count.get());
    assertEquals("foo", completion.get());
    assertEquals(1, count.get());
    assertEquals(null, completion.getCause());
    assertEquals(1, count.get());
    assertEquals(false, completion.isFailed());
    assertEquals(1, count.get());
  }

  @Test
  public void testFutureFails() {
    final Exception reason = new Exception();
    final AtomicInteger count = new AtomicInteger();
    Completion<String> completion = Completion.future(new Callable<String>() {
      public String call() throws Exception {
        count.incrementAndGet();
        throw reason;
      }
    });
    assertEquals(0, count.get());
    assertEquals(null, completion.get());
    assertEquals(1, count.get());
    assertEquals(null, completion.get());
    assertEquals(1, count.get());
    assertEquals(reason, completion.getCause());
    assertEquals(1, count.get());
    assertEquals(true, completion.isFailed());
    assertEquals(1, count.get());
  }
}
