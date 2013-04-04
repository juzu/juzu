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

package juzu.test.protocol.http;

import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.test.AbstractWebTestCase;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractHttpTestCase extends AbstractWebTestCase {

  public static ApplicationLifeCycle<?, ?> getCurrentApplication() throws IllegalStateException {
    throw new UnsupportedOperationException();
  }

  public static WebArchive createDeployment(String applicationName) {
    URL jquery = HttpServletImpl.class.getResource("jquery-1.7.1.js");
    URL test = HttpServletImpl.class.getResource("test.js");
    URL css = HttpServletImpl.class.getResource("main.css");
    URL less = HttpServletImpl.class.getResource("main.less");
    return createServletDeployment(true, applicationName).
      addAsWebResource(jquery, "jquery.js").
      addAsWebResource(test, "test.js").
      addAsWebResource(css, "main.css").
      addAsWebResource(css, "main.less");
  }
}
