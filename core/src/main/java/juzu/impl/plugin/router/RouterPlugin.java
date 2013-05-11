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

package juzu.impl.plugin.router;

import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterPlugin extends ApplicationPlugin {

  /** . */
  private RouteDescriptor descriptor;

  public RouterPlugin() {
    super("router");
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    if (context.getConfig() != null) {
      descriptor = new RouteDescriptor(context.getConfig());
    }
    return descriptor;
  }

  public RouteDescriptor getDescriptor() {
    return descriptor;
  }
}
