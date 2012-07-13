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

package juzu.impl.application.metamodel;

import juzu.Application;
import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.controller.metamodel.ControllersMetaModel;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.template.metamodel.TemplatesMetaModel;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModelObject {

  /** . */
  public static final MessageCode CANNOT_WRITE_APPLICATION_CONFIG = new MessageCode("CANNOT_WRITE_APPLICATION_CONFIG", "The application %1$s configuration cannot be written");

  /** . */
  public static final MessageCode CANNOT_WRITE_CONFIG = new MessageCode("CANNOT_WRITE_CONFIG", "The configuration cannot be written");

  /** . */
  final ElementHandle.Package handle;

  /** . */
  final FQN fqn;

  /** . */
  public MetaModel model;

  /** . */
  boolean modified;

  /** . */
  final Map<BufKey, AnnotationData> toProcess;

  /** . */
  final Map<BufKey, AnnotationData> processed;

  /** . */
  final String baseName;

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
    String name = baseName + "Application";
    FQN fqn = new FQN(handle.getQN(), name);

    //
    this.handle = handle;
    this.fqn = fqn;
    this.modified = false;
    this.baseName = baseName;
    this.toProcess = new HashMap<BufKey, AnnotationData>();
    this.processed = new HashMap<BufKey, AnnotationData>();
  }

  public FQN getFQN() {
    return fqn;
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
    json.set("fqn", fqn.getName());
    json.map("templates", getChild(TemplatesMetaModel.KEY));
    json.map("controllers", getChild(ControllersMetaModel.KEY));
    return json;
  }

  @Override
  public boolean exist(MetaModel model) {
    PackageElement element = model.env.get(handle);
    boolean found = false;
    if (element != null) {
      for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
        if (found = ((TypeElement)annotationMirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(Application.class.getName())) {
          break;
        }
      }
    }
    return found;
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationsMetaModel) {
      queue(MetaModelEvent.createAdded(this));
      ApplicationsMetaModel applications = (ApplicationsMetaModel)parent;
      model = applications.model;

      //
      for (ApplicationMetaModelPlugin plugin : applications.plugins.values()) {
        plugin.postConstruct(this);
      }
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ApplicationsMetaModel) {
      ApplicationsMetaModel applications = (ApplicationsMetaModel)parent;

      //
      for (ApplicationMetaModelPlugin plugin : applications.plugins.values()) {
        plugin.preDestroy(this);
      }

      //
      applications.toProcess.putAll(processed);
      toProcess.clear();

      //
      queue(MetaModelEvent.createRemoved(this));
      model = null;
    }
  }
}
