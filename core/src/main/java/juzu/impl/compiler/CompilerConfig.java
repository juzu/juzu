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
