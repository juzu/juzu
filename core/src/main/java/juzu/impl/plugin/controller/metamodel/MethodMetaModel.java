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
      id = handle.getTypeName().getIdentifier() + "." + handle.getName();
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

  public ParameterMetaModel parameterAt(int index) {
    return parameters.get(index);
  }

  public ParameterMetaModel parameterBy(String name) throws NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null name allowed");
    }
    for (ParameterMetaModel parameter : parameters) {
      if (parameter.getName().equals(name)) {
        return parameter;
      }
    }
    return null;
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
