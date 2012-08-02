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

package juzu.impl.plugin.controller.metamodel;

import juzu.AmbiguousResolutionException;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;

import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersMetaModel extends MetaModelObject implements Iterable<ControllerMetaModel> {

  /** . */
  public final static Key<ControllersMetaModel> KEY = Key.of(ControllersMetaModel.class);

  /** . */
  FQN defaultController;

  /** . */
  Boolean escapeXML;

  /** . */
  private ApplicationMetaModel application;

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(ControllerMetaModel.class));
    return json;
  }

  public ApplicationMetaModel getApplication() {
    return application;
  }

  public Iterator<ControllerMetaModel> iterator() {
    return getChildren(ControllerMetaModel.class).iterator();
  }

  public ControllerMetaModel get(ElementHandle.Class handle) {
    return getChild(Key.of(handle, ControllerMetaModel.class));
  }

  public void add(ControllerMetaModel controller) {
    addChild(Key.of(controller.handle, ControllerMetaModel.class), controller);
  }

  public void remove(ControllerMetaModel controller) {
    if (controller.controllers != this) {
      throw new IllegalArgumentException();
    }
    removeChild(Key.of(controller.handle, ControllerMetaModel.class));
  }

  public MethodMetaModel resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException {
    try {
      ControllerMetaModelResolver resolver = new ControllerMetaModelResolver(this);
      return resolver.resolve(typeName, methodName, parameterNames);
    }
    catch (AmbiguousResolutionException e) {
      // RootMetaModel.log.log("Could not resolve ambiguous method " + methodName + " " + parameterNames);
      return null;
    }
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      application = (ApplicationMetaModel)parent;
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      application = null;
    }
  }
}
