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

package juzu.impl.plugin;

/**
 * Base class for a plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Service {

  /** The plugin name. */
  private final String name;

  protected Service(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Returns the plugin descriptor or null if the plugin should not be loaded.
   *
   * @param context the plugin context
   * @return the descriptor
   * @throws Exception any exception
   */
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    return null;
  }
}
