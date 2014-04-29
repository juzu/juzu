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

import juzu.impl.common.JSON;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.ApplicationService;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterService extends ApplicationService {

  /** . */
  private RouterDescriptor descriptor;

  public RouterService() {
    super("router");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    if (context.getConfig() != null) {
      List<? extends JSON> routesConfig = context.getConfig().getList("routes", JSON.class);
      RouterDescriptor routerDescriptor = new RouterDescriptor();
      for (JSON routeConfig : routesConfig) {
        RouteDescriptor routeDescriptor = new RouteDescriptor(routeConfig);
        routerDescriptor.routes.add(routeDescriptor);
      }
      descriptor = routerDescriptor;
    }
    return descriptor;
  }

  public RouterDescriptor getDescriptor() {
    return descriptor;
  }
}
