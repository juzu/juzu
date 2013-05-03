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

import juzu.request.RequestParameter;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LexersTestCase extends AbstractTestCase {



  @Test
  public void testParseQueryString() {

    class Assert {

      final Iterator<RequestParameter> i;

      Assert(String s) {
        this.i = Lexers.queryParser(s, 0, s.length());
      }

      Assert assertParameter(String expectedName, String... expectedValue) {
        RequestParameter expected = RequestParameter.create(expectedName, expectedValue);
        if (i.hasNext()) {
          RequestParameter next = i.next();
          assertEquals(expected, next);
        } else {
          throw failure("Was expecting another parameter equals to " + expected);
        }
        return this;
      }

      void assertDone() {
        if (i.hasNext()) {
          throw failure("Was not expecting those parameters " + Tools.list(i));
        }
      }
    }

    // Empty
    new Assert("").assertDone();

    // One param
    new Assert("f").assertParameter("f", "").assertDone();
    new Assert("f=").assertParameter("f", "").assertDone();
    new Assert("f=b").assertParameter("f", "b").assertDone();
    new Assert("f=bar").assertParameter("f", "bar").assertDone();
    new Assert("foo").assertParameter("foo", "").assertDone();
    new Assert("foo=").assertParameter("foo", "").assertDone();
    new Assert("foo=b").assertParameter("foo", "b").assertDone();
    new Assert("foo=bar").assertParameter("foo", "bar").assertDone();

    // Two values
    new Assert("f&f=bar2").assertParameter("f", "").assertParameter("f", "bar2").assertDone();
    new Assert("f=&f=bar2").assertParameter("f", "").assertParameter("f", "bar2").assertDone();
    new Assert("f=b&f=bar2").assertParameter("f", "b").assertParameter("f", "bar2").assertDone();
    new Assert("f=bar&f=bar2").assertParameter("f", "bar").assertParameter("f", "bar2").assertDone();
    new Assert("foo&foo=bar2").assertParameter("foo", "").assertParameter("foo", "bar2").assertDone();
    new Assert("foo=&foo=bar2").assertParameter("foo", "").assertParameter("foo", "bar2").assertDone();
    new Assert("foo=b&foo=bar2").assertParameter("foo", "b").assertParameter("foo", "bar2").assertDone();
    new Assert("foo=bar&foo=bar2").assertParameter("foo", "bar").assertParameter("foo", "bar2").assertDone();
    new Assert("f=bar2&f").assertParameter("f", "bar2").assertParameter("f", "").assertDone();
    new Assert("f=bar2&f=").assertParameter("f", "bar2").assertParameter("f", "").assertDone();
    new Assert("f=bar2&f=b").assertParameter("f", "bar2").assertParameter("f", "b").assertDone();
    new Assert("f=bar2&f=bar").assertParameter("f", "bar2").assertParameter("f", "bar").assertDone();
    new Assert("foo=bar2&foo").assertParameter("foo", "bar2").assertParameter("foo", "").assertDone();
    new Assert("foo=bar2&foo=").assertParameter("foo", "bar2").assertParameter("foo", "").assertDone();
    new Assert("foo=bar2&foo=b").assertParameter("foo", "bar2").assertParameter("foo", "b").assertDone();
    new Assert("foo=bar2&foo=bar").assertParameter("foo", "bar2").assertParameter("foo", "bar").assertDone();

    // Encoded name
    new Assert("+=foo").assertParameter("+", "foo").assertDone();
    new Assert(".=foo").assertParameter(".", "foo").assertDone();
    new Assert("-=foo").assertParameter("-", "foo").assertDone();
    new Assert("*=foo").assertParameter("*", "foo").assertDone();
    new Assert("_=foo").assertParameter("_", "foo").assertDone();
    new Assert("/=foo").assertParameter("/", "foo").assertDone();
    new Assert("%2F=foo").assertParameter("/", "foo").assertDone();

    // Encoded value
    new Assert("foo=+").assertParameter("foo", "+").assertDone();
    new Assert("foo=.").assertParameter("foo", ".").assertDone();
    new Assert("foo=-").assertParameter("foo", "-").assertDone();
    new Assert("foo=*").assertParameter("foo", "*").assertDone();
    new Assert("foo=_").assertParameter("foo", "_").assertDone();
    new Assert("foo=/").assertParameter("foo", "/").assertDone();
    new Assert("foo=%2F").assertParameter("foo", "/").assertDone();

    // Malformed name
    new Assert("%2=foo").assertDone();
    new Assert("foo&%2=foo").assertParameter("foo", "").assertDone();
    new Assert("foo=bar&%2=foo").assertParameter("foo", "bar").assertDone();
    new Assert("%2=foo&foo").assertParameter("foo", "").assertDone();

    // Malformed value
    new Assert("foo=%2").assertDone();
    new Assert("foo&foo=%2").assertParameter("foo", "").assertDone();
    new Assert("foo=bar&foo=%2").assertParameter("foo", "bar").assertDone();
    new Assert("foo=%2&foo").assertParameter("foo", "").assertDone();

    // Two params
    new Assert("x=y&f").assertParameter("x", "y").assertParameter("f", "").assertDone();
    new Assert("x=y&f=").assertParameter("x", "y").assertParameter("f", "").assertDone();
    new Assert("x=y&f=b").assertParameter("x", "y").assertParameter("f", "b").assertDone();
    new Assert("x=y&f=bar").assertParameter("x", "y").assertParameter("f", "bar").assertDone();
    new Assert("x=y&foo").assertParameter("x", "y").assertParameter("foo", "").assertDone();
    new Assert("x=y&foo=").assertParameter("x", "y").assertParameter("foo", "").assertDone();
    new Assert("x=y&foo=b").assertParameter("x", "y").assertParameter("foo", "b").assertDone();
    new Assert("x=y&foo=bar").assertParameter("x", "y").assertParameter("foo", "bar").assertDone();

    // Value containing '='
    new Assert("f=b=j").assertParameter("f", "b=j").assertDone();
    new Assert("f=bar=j").assertParameter("f", "bar=j").assertDone();
    new Assert("f=b=juu").assertParameter("f", "b=juu").assertDone();
    new Assert("f=bar=juu").assertParameter("f", "bar=juu").assertDone();
    new Assert("foo=b=j").assertParameter("foo", "b=j").assertDone();
    new Assert("foo=bar=j").assertParameter("foo", "bar=j").assertDone();
    new Assert("foo=b=juu").assertParameter("foo", "b=juu").assertDone();
    new Assert("foo=bar=juu").assertParameter("foo", "bar=juu").assertDone();

    // Invalid chunk
    new Assert("=").assertDone();
    new Assert("=x").assertDone();
    new Assert("=x=").assertDone();
    new Assert("=x=y").assertDone();

    // Invalid chunk with ampersand
    new Assert("=&").assertDone();
    new Assert("=x&").assertDone();
    new Assert("=x=&").assertDone();
    new Assert("=x=y&").assertDone();
    new Assert("=&f").assertParameter("f", "").assertDone();
    new Assert("=x&f").assertParameter("f", "").assertDone();
    new Assert("=x=&f").assertParameter("f", "").assertDone();
    new Assert("=x=y&f").assertParameter("f", "").assertDone();
    new Assert("=&f=b").assertParameter("f", "b").assertDone();
    new Assert("=x&f=b").assertParameter("f", "b").assertDone();
    new Assert("=x=&f=b").assertParameter("f", "b").assertDone();
    new Assert("=x=y&f=b").assertParameter("f", "b").assertDone();
    new Assert("=&foo").assertParameter("foo", "").assertDone();
    new Assert("=x&foo").assertParameter("foo", "").assertDone();
    new Assert("=x=&foo").assertParameter("foo", "").assertDone();
    new Assert("=x=y&foo").assertParameter("foo", "").assertDone();
    new Assert("=&foo=").assertParameter("foo", "").assertDone();
    new Assert("=x&foo=").assertParameter("foo", "").assertDone();
    new Assert("=x=&foo=").assertParameter("foo", "").assertDone();
    new Assert("=x=y&foo=").assertParameter("foo", "").assertDone();
    new Assert("=&foo=b").assertParameter("foo", "b").assertDone();
    new Assert("=x&foo=b").assertParameter("foo", "b").assertDone();
    new Assert("=x=&foo=b").assertParameter("foo", "b").assertDone();
    new Assert("=x=y&foo=b").assertParameter("foo", "b").assertDone();
    new Assert("=&foo=bar").assertParameter("foo", "bar").assertDone();
    new Assert("=x&foo=bar").assertParameter("foo", "bar").assertDone();
    new Assert("=x=&foo=bar").assertParameter("foo", "bar").assertDone();
    new Assert("=x=y&foo=bar").assertParameter("foo", "bar").assertDone();

    //
    assertEquals("%2B", Lexers.queryParser("a=%2B").next().getRaw(0));
    assertEquals("+", Lexers.queryParser("a=%2B").next().get(0));
  }
}
