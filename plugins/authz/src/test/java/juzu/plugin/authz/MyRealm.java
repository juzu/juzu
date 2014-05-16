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
package juzu.plugin.authz;

import org.apache.catalina.Container;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

import java.security.Principal;
import java.util.Arrays;

/**
 * @author Julien Viet
 */
public class MyRealm extends RealmBase {

  @Override
  protected String getName() {
    return "AuthenticationRealm";
  }

  @Override
  public void setContainer(Container container) {
    super.setContainer(container);

    // Need to set container log
    this.containerLog = container.getLogger();
  }

  @Override
  protected String getPassword(String username) {
    return username;
  }

  @Override
  protected Principal getPrincipal(String username) {
    return new GenericPrincipal(username, username, Arrays.asList("myrole"));
  }
}
