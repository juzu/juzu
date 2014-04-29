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

package juzu.plugin.portlet.impl;

import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.ApplicationService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletService extends ApplicationService {

  public PortletService() {
    super("portlet");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    return PortletDescriptor.INSTANCE;
  }
}
