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

package juzu.impl.inject.spi.spring;

import juzu.impl.inject.ScopeController;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SpringScope implements Scope {

  /** . */
  private final DefaultListableBeanFactory factory;

  /** . */
  private final juzu.Scope scope;

  /** . */
  private final ScopeController controller;

  SpringScope(DefaultListableBeanFactory factory, juzu.Scope scope, ScopeController controller) {
    this.factory = factory;
    this.scope = scope;
    this.controller = controller;
  }

  public Object get(String name, ObjectFactory<?> objectFactory) {
    SpringScoped scoped = (SpringScoped)controller.get(scope, name);
    if (scoped == null) {
      // We register first the scoped object
      // as creating the object from the factory
      // will make a call in the method registerDestructionCallback
      scoped = new SpringScoped(factory, name);
      controller.put(scope, name, scoped);

      // Create the object, it will likely create a registerDestructionCallback invocation
      // to set the callback when the object will need to be destroyed
      scoped.o = objectFactory.getObject();
    }
    return scoped.o;
  }

  public Object remove(String name) {
    SpringScoped scoped = (SpringScoped)controller.get(scope, name);
    return scoped != null ? scoped.o : null;
  }

  public void registerDestructionCallback(String name, Runnable callback) {
    SpringScoped scoped = (SpringScoped)controller.get(scope, name);
    if (scoped != null) {
      scoped.destructionCallback = callback;
    }
  }

  public Object resolveContextualObject(String key) {
    throw new UnsupportedOperationException();
  }

  public String getConversationId() {
    return "foo"; // ????
  }
}
