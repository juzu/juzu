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

package juzu.impl.application.metamodel;

import juzu.Application;
import juzu.impl.common.QN;
import juzu.impl.compiler.Annotation;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.controller.metamodel.ControllersMetaModel;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.template.metamodel.TemplatesMetaModel;
import juzu.impl.common.JSON;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModel<ApplicationMetaModelPlugin, ApplicationMetaModel> {

  /** . */
  public static final MessageCode CANNOT_WRITE_APPLICATION_CONFIG = new MessageCode("CANNOT_WRITE_APPLICATION_CONFIG", "The application %1$s configuration cannot be written");

  /** . */
  public static final MessageCode CANNOT_WRITE_CONFIG = new MessageCode("CANNOT_WRITE_CONFIG", "The configuration cannot be written");

  /** . */
  final ElementHandle.Package handle;

  /** . */
  public ApplicationsMetaModel model;

  /** . */
  boolean modified;

  /** . */
  final Map<BufKey, Annotation> toProcess;

  /** . */
  final Map<BufKey, Annotation> processed;

  /** . */
  final String baseName;

  /** . */
  private ApplicationsMetaModel applications;

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

    //
    this.handle = handle;
    this.modified = false;
    this.baseName = baseName;
    this.toProcess = new HashMap<BufKey, Annotation>();
    this.processed = new HashMap<BufKey, Annotation>();
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
  public boolean exist(ProcessingContext env) {
    PackageElement element = env.get(handle);
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

  void processEvents(Collection<ApplicationMetaModelPlugin> plugins) {
    for (ApplicationMetaModelPlugin plugin : plugins) {
      plugin.processEvents(this, new EventQueue(dispatch));
    }

    // Clear dispatch queue
    dispatch.clear();
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationsMetaModel) {
      applications = (ApplicationsMetaModel)parent;
      model = applications;
      applications.added(this);
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof ApplicationsMetaModel) {
      ApplicationsMetaModel applications = (ApplicationsMetaModel)parent;
      applications.removed(this);
      this.model = null;
      this.applications = null;
    }
  }
}
