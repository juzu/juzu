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

import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.request.Phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodMetaModel extends MetaModelObject {

  /** The controller. */
  ControllerMetaModel controller;

  /** . */
  final ElementHandle.Method handle;

  /** . */
  final String declaredId;

  /** . */
  final Phase phase;

  /** . */
  final String name;

  /** . */
  final ArrayList<ParameterMetaModel> parameters;

  /** . */
  final String id;

  MethodMetaModel(
      ElementHandle.Method handle,
      String declaredId,
      Phase phase,
      String name,
      ArrayList<ParameterMetaModel> parameters) {

    String id;
    if (declaredId == null) {
      id = handle.getFQN().getSimpleName() + "." + handle.getName();
    } else {
      id = declaredId;
    }

    //
    this.handle = handle;
    this.declaredId = declaredId;
    this.phase = phase;
    this.name = name;
    this.parameters = parameters;
    this.id = id;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("handle", handle);
    json.set("id", declaredId);
    json.set("phase", phase);
    json.set("name", name);
    json.map("parameters", new ArrayList<ParameterMetaModel>(parameters));
    return json;
  }

  public ControllerMetaModel getController() {
    return controller;
  }

  public ElementHandle.Method getHandle() {
    return handle;
  }

  public String getId() {
    return id;
  }

  public Phase getPhase() {
    return phase;
  }

  public String getName() {
    return name;
  }

  public ArrayList<ParameterMetaModel> getParameters() {
    return parameters;
  }

  public ParameterMetaModel getParameter(int index) {
    return parameters.get(index);
  }

  public Set<String> getParameterNames() {
    Set<String> tmp = new HashSet<String>();
    for (ParameterMetaModel param : parameters) {
      tmp.add(param.getName());
    }
    return tmp;
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    queue(MetaModelEvent.createRemoved(this, controller.getControllers().getApplication().getHandle()));
    controller = null;
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    controller = (ControllerMetaModel)parent;
    queue(MetaModelEvent.createAdded(this));
  }
}
