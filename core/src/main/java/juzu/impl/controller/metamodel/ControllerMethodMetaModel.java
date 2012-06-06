/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.controller.metamodel;

import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Tools;
import juzu.request.Phase;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMethodMetaModel extends MetaModelObject {

  /** The controller. */
  ControllerMetaModel controller;

  /** . */
  final ElementHandle.Method handle;

  /** . */
  final String id;

  /** . */
  final Phase phase;

  /** . */
  final String name;

  /** . */
  final ArrayList<ParameterMetaModel> parameters;

  ControllerMethodMetaModel(
    ElementHandle.Method handle,
    String id,
    Phase phase,
    String name,
    ArrayList<ParameterMetaModel> parameters) {
    this.handle = handle;
    this.id = id;
    this.phase = phase;
    this.name = name;
    this.parameters = parameters;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("handle", handle);
    json.set("id", id);
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
  protected void postAttach(MetaModelObject parent) {
    controller = (ControllerMetaModel)parent;
  }

  @Override
  public boolean exist(MetaModel model) {
    ExecutableElement methodElt = model.env.get(handle);
    if (methodElt != null) {
      AnnotationMirror am = Tools.getAnnotation(methodElt, phase.annotation.getName());
      if (am != null) {
        AnnotationData values = AnnotationData.create(am);
        String id = (String)values.get("id");
        return Tools.safeEquals(id, this.id);
      }
    }
    return false;
  }
}
