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

package juzu.impl.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerConfig {

  /** . */
  private static final Map<String, String> EMPTY = Collections.emptyMap();

  /** . */
  private Map<String, String> processorOptions = EMPTY;

  /** . */
  private boolean force;

  public CompilerConfig withProcessorOption(String optionName, String optionValue) {
    if (optionValue != null) {
      if (processorOptions == EMPTY) {
        processorOptions = new HashMap<String, String>();
      }
      processorOptions.put(optionName, optionValue);
    }
    else {
      if (processorOptions != EMPTY) {
        processorOptions.remove(optionName);
      }
    }
    return this;
  }

  public Iterable<String> getProcessorOptionNames() {
    return processorOptions.keySet();
  }

  public String getProcessorOptionValue(String optionName) {
    return processorOptions.get(optionName);
  }

  public boolean getForce() {
    return force;
  }

  public CompilerConfig force(boolean force) {
    this.force = force;
    return this;
  }
}
