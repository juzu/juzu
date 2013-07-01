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

package juzu.impl.bridge;

import juzu.impl.common.Name;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BridgeConfig {

  /** . */
  public static final String RUN_MODE = "juzu.run_mode";

  /** . */
  public static final String INJECT = "juzu.inject";

  /** . */
  public static final String APP_NAME = "juzu.app_name";

  /** . */
  public static final String SOURCE_PATH = "juzu.src_path";

  /** The default request encoding charset. */
  public static final String REQUEST_ENCODING = "juzu.request_encoding";

  /** . */
  public static final Set<String> NAMES = Collections.unmodifiableSet(Tools.set(INJECT, APP_NAME, REQUEST_ENCODING));

  /** . */
  public final Name name;

  /** . */
  public final InjectorProvider injectorProvider;

  /** . */
  public final Charset requestEncoding;

  public static Name getApplicationName(Map<String, String> config) {
    String applicationName = config.get(APP_NAME);
    return applicationName != null ? Name.parse(applicationName) : null;
  }

  public static InjectorProvider getInjectImplementation(Map<String, String> config) throws Exception {
    String inject = config.get(INJECT);
    InjectorProvider implementation;
    if (inject == null) {
      implementation = InjectorProvider.INJECT_GUICE;
    } else {
      inject = inject.trim().toLowerCase();
      implementation = InjectorProvider.find(inject);
      if (implementation == null) {
        throw new Exception("unrecognized inject vendor " + inject);
      }
    }
    return implementation;
  }

  public static Charset getRequestEncoding(Map<String, String> config) {
    String requestEncodingParam = config.get(REQUEST_ENCODING);
    return requestEncodingParam != null ? Charset.forName(requestEncodingParam) : Tools.ISO_8859_1;
  }

  public BridgeConfig(Map<String, String> config) throws Exception {
    this.name = getApplicationName(config);
    this.injectorProvider = getInjectImplementation(config);
    this.requestEncoding = getRequestEncoding(config);
  }
}
