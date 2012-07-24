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
