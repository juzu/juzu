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

import juzu.impl.common.JSON;
import juzu.impl.resource.ResourceResolver;

/**
 * The context of a plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface PluginContext {

  /**
   * Returns the plugin configuration.
   *
   * @return the plugin configuraiton
   */
  JSON getConfig();

  /**
   * Reurns the classloader.
   *
   * @return the classloader
   */
  ClassLoader getClassLoader();

  /**
   * Returns the resource resolver for server resources.
   *
   * @return the server resolver
   */
  ResourceResolver getServerResolver();

  /**
   * Returns the resource resolver for applications resources.
   *
   * @return the application resolver
   */
  ResourceResolver getApplicationResolver();

}
