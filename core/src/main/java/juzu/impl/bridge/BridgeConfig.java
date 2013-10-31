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

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BridgeConfig {

  /** . */
  public static final String INJECT = "juzu.inject";

  /** . */
  public static final String APP_NAME = "juzu.app_name";

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

  public BridgeConfig(Logger log, Map<String, String> config) throws Exception {
    this.name = getApplicationName(config);
    this.injectorProvider = getInjectImplementation(log, config);
    this.requestEncoding = getRequestEncoding(config);
  }

  private Name getApplicationName(Map<String, String> config) {
    String applicationName = config.get(APP_NAME);
    return applicationName != null ? Name.parse(applicationName) : null;
  }

  private InjectorProvider getInjectImplementation(Logger log, Map<String, String> config) throws Exception {
    String inject = config.get(INJECT);
    if (inject == null) {
      log.debug("No inject implementation specified will detect one available");
      TreeMap<Integer, InjectorProvider> providers = new TreeMap<Integer, InjectorProvider>();
      for (InjectorProvider provider : InjectorProvider.values()) {
        if (provider.isAvailable()) {
          log.debug("Inject implementation " + provider.getValue() + " available");
          providers.put(provider.getPriority(), provider);
        } else {
          log.debug("Inject implementation " + provider.getValue() + " not available");
        }
      }
      Iterator<InjectorProvider> i = providers.values().iterator();
      if (i.hasNext()) {
        InjectorProvider implementation = i.next();
        log.debug("Selected " + implementation.get() + " inject implementation");
        return implementation;
      } else {
        log.debug("No inject implementation available");
        return null;
      }
    } else {
      inject = inject.trim().toLowerCase();
      InjectorProvider implementation = InjectorProvider.find(inject);
      if (implementation == null) {
        log.debug("Inject implementation " + inject + " not available");
        return null;
      } else {
        return implementation;
      }
    }
  }

  private Charset getRequestEncoding(Map<String, String> config) {
    String requestEncodingParam = config.get(REQUEST_ENCODING);
    if (requestEncodingParam != null) {
      requestEncodingParam = Tools.interpolate(requestEncodingParam, System.getProperties());
      return Charset.forName(requestEncodingParam);
    } else {
      return Tools.ISO_8859_1;
    }
  }
}
