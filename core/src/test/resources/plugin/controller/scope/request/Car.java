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

package plugin.controller.scope.request;

import juzu.RequestScoped;
import juzu.test.Identifiable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RequestScoped
public class Car implements Identifiable {

  /** . */
  private int status = CONSTRUCTED;

  public long getIdentityHashCode() {
    return System.identityHashCode(this);
  }

  @PostConstruct
  public void create() {
    status = MANAGED;
  }

  @PreDestroy
  public void destroy() {
    status = DESTROYED;
  }

  public int getStatus() {
    return status;
  }
}
