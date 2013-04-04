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

package juzu.impl.plugin.application.metamodel;

import juzu.impl.common.Name;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.template.metamodel.TemplatesMetaModel;
import juzu.impl.common.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModel<ApplicationMetaModelPlugin, ApplicationMetaModel> {

  /** . */
  public static final MessageCode CANNOT_WRITE_APPLICATION_CONFIG = new MessageCode("CANNOT_WRITE_APPLICATION_CONFIG", "The application %1$s configuration cannot be written");

  /** . */
  public static final MessageCode CANNOT_WRITE_CONFIG = new MessageCode("CANNOT_WRITE_CONFIG", "The configuration cannot be written");

  /** . */
  final ElementHandle.Package handle;

  /** . */
  public ModuleMetaModel model;

  /** . */
  boolean modified;

  /** . */
  final String baseName;

  ApplicationMetaModel(
    ElementHandle.Package handle,
    String baseName) {
    //
    if (baseName == null) {
      String s = handle.getPackage().toString();
      int index = s.lastIndexOf('.');
      baseName = Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
    }

    //
    this.handle = handle;
    this.modified = false;
    this.baseName = baseName;
  }

  public Name getName() {
    return handle.getPackage();
  }

  public String getBaseName() {
    return baseName;
  }

  public ElementHandle.Package getHandle() {
    return handle;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("handle", handle);
    json.set("qn", handle.getPackage().toString());
    json.map("templates", getChild(TemplatesMetaModel.KEY));
    json.map("controllers", getChild(ControllersMetaModel.KEY));
    return json;
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ModuleMetaModel) {
      model = (ModuleMetaModel)parent;
      model.queue(MetaModelEvent.createAdded(this));
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ModuleMetaModel) {
      ModuleMetaModel applications = (ModuleMetaModel)parent;
      applications.queue(MetaModelEvent.createRemoved(this));
      this.model = null;
    }
  }
}
