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

package plugin.controller.scope.flash;

import juzu.Action;
import juzu.View;
import juzu.test.Registry;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Action
  public void action() {
    try {
      long code = car.getIdentityHashCode();
      Registry.set("car", code);
      Registry.set("status", car.getStatus());
    }
    catch (ContextNotActiveException expected) {
    }
  }

  @Inject
  private Car car;

  @View
  public void index() {
    Registry.set("car", car.getIdentityHashCode());
    Registry.set("status", car.getStatus());
    Registry.set("action", A_.action().toString());
//      Registry.set("resource", A_.resourceURL().toString());
  }

/*
   @Resource
   public void resource()
   {
      try
      {
         long code = car.getIdentityHashCode();
         Registry.set("car", code);
      }
      catch (ContextNotActiveException expected)
      {
      }
   }
*/
}
