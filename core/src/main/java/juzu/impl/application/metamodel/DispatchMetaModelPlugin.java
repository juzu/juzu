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
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.AnnotationChange;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModelContext;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;

import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DispatchMetaModelPlugin extends ApplicationsMetaModelPlugin {

  /** . */
  final MetaModelContext<ApplicationMetaModelPlugin, ApplicationMetaModel> context;

  public DispatchMetaModelPlugin() {
    super("applications");

    //
    this.context = new MetaModelContext<ApplicationMetaModelPlugin, ApplicationMetaModel>(ApplicationMetaModelPlugin.class);
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {

    //
    context.init(env);

    // We are interested by the Application annotation
    HashSet<Class<? extends java.lang.annotation.Annotation>> annotationTypes = new HashSet<Class<? extends java.lang.annotation.Annotation>>();
    annotationTypes.add(Application.class);
    annotationTypes.addAll(context.getSupportedAnnotations());

    //
    return annotationTypes;
  }

  @Override
  public void init(ApplicationsMetaModel metaModel) {
    metaModel.applicationContext = context;
  }

  @Override
  public void postActivate(ApplicationsMetaModel metaModel) {
    for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
      plugin.postActivate(metaModel);
    }

    //
    context.postActivate(metaModel.env);
    for (ApplicationMetaModel application : metaModel) {
      for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
        plugin.postActivate(application);
      }
    }
  }

  @Override
  public void processAnnotationChanges(ApplicationsMetaModel metaModel, Iterable<AnnotationChange> changes) {

    // Normal processing for now
    super.processAnnotationChanges(metaModel, changes);

    // Forward
    metaModel.applicationContext.processAnnotationChanges(changes);
  }

  @Override
  public void processAnnotationAdded(ApplicationsMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(ApplicationsMetaModel.APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      String name = (String)added.get("name");
      metaModel.add(pkg, name);
    }
  }

  @Override
  public void processAnnotationUpdated(ApplicationsMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (key.getType().equals(ApplicationsMetaModel.APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      ApplicationMetaModel application = metaModel.get(pkg);
      application.modified = true;
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationsMetaModel metaModel, AnnotationKey key, AnnotationState state) {
    if (key.getType().equals(ApplicationsMetaModel.APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      ApplicationMetaModel mm = metaModel.get(pkg);
      if (mm != null) {
        mm.remove();
      }
    }
  }

  @Override
  public void postProcessAnnotations(ApplicationsMetaModel metaModel) {
    metaModel.resolveApplications();
    context.postProcessAnnotations();
  }

  @Override
  public void processEvents(ApplicationsMetaModel metaModel, EventQueue queue) {
    // Handle root events
    while (queue.hasEvents()) {
      MetaModelEvent event = queue.popEvent();
      MetaModelObject obj = event.getObject();
      if (obj instanceof ApplicationMetaModel) {
        ApplicationMetaModel application = (ApplicationMetaModel)obj;
        if (event.getType() == MetaModelEvent.AFTER_ADD) {
          metaModel.emitApplication(metaModel.env, application);
        }
      }
    }

    // Distribute per application nevents
    context.processEvents();
  }

  @Override
  public void postProcessEvents(ApplicationsMetaModel metaModel) {
    context.postProcessEvents();
  }

  @Override
  public void prePassivate(ApplicationsMetaModel metaModel) {
    context.prePassivate();

    //
    for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
      plugin.prePassivate(metaModel);
    }

    //
    metaModel.env.log("Emitting config");
    metaModel.emitConfig();
  }
}
