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

package juzu.impl.bridge.request;

import juzu.io.Encoding;
import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractRequestQueryParamEncoding extends AbstractWebTestCase {

  public static final Encoding CUSTOM = new Encoding("CUSTOM") {
    @Override
    public void encodeSegment(CharSequence s, Appendable appendable) throws IOException {
      Encoding.RFC3986.encodeSegment(s, appendable);
    }

    @Override
    public void encodeQueryParamName(CharSequence s, Appendable appendable) throws IOException {
      Encoding.RFC3986.encodeQueryParamValue(s, appendable);
    }

    @Override
    public void encodeQueryParamValue(CharSequence s, Appendable appendable) throws IOException {
      appendable.append("<");
      Encoding.RFC3986.encodeQueryParamValue(s, appendable);
      appendable.append(">");
    }
  };

  /** . */
  public static String value;

  @Drone
  WebDriver driver;

  @Test
  public void testPathParam() throws Exception {
    driver.get(applicationURL().toString());
    URL url = new URL(value);
    assertEquals("encoded=<(:,)>", url.getQuery());
  }
}
