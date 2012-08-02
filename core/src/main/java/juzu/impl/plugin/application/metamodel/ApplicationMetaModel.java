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

package juzu.impl.plugin.application.metamodel;

import juzu.impl.common.QN;
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

  /** . */
  private ModuleMetaModel applications;

  ApplicationMetaModel(
    ElementHandle.Package handle,
    String baseName) {
    //
    if (baseName == null) {
      String s = handle.getQN().getValue();
      int index = s.lastIndexOf('.');
      baseName = Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
    }

    //
    this.handle = handle;
    this.modified = false;
    this.baseName = baseName;
  }

  public QN getName() {
    return handle.getQN();
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
    json.set("qn", handle.getQN().toString());
    json.map("templates", getChild(TemplatesMetaModel.KEY));
    json.map("controllers", getChild(ControllersMetaModel.KEY));
    return json;
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ModuleMetaModel) {
      applications = (ModuleMetaModel)parent;
      model = applications;
      applications.queue(MetaModelEvent.createAdded(this));
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ModuleMetaModel) {
      ModuleMetaModel applications = (ModuleMetaModel)parent;
      applications.queue(MetaModelEvent.createRemoved(this));
      this.model = null;
      this.applications = null;
    }
  }
}
