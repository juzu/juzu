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
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModelContext;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
    metaModel.context = context;
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
  public void processAnnotation(ApplicationsMetaModel metaModel, Element element, Annotation annotation) {
    PackageElement pkg = metaModel.env.getPackageOf(element);
    QN pkgQN = QN.parse(pkg.getQualifiedName());

    //
    ApplicationMetaModel found = null;

    //
    if (annotation.getName().equals(ApplicationsMetaModel.APPLICATION)) {
      found = metaModel.processApplication((PackageElement)element, annotation);

      // Process this annotation manually
      for (ApplicationMetaModelPlugin plugin :context.getPlugins()) {
        plugin.processAnnotation(found, element, annotation);
      }
    }
    else {
      for (ApplicationMetaModel application : metaModel) {
        if (application.getName().isPrefix(pkgQN)) {
          found = application;
          break;
        }
      }

      //
      BufKey key = new BufKey(metaModel.env, element, annotation.getName());
      if (found == null) {
        metaModel.toProcess.put(key, annotation);
        metaModel.env.log("Buffering " + key + " = " + annotation);
      }
      else {
        found.toProcess.put(key, annotation);
      }
    }

    // Broadcast annotations
    if (found != null) {
      for (Iterator<Map.Entry<BufKey, Annotation>> i = found.toProcess.entrySet().iterator();i.hasNext();) {
        Map.Entry<BufKey, Annotation> entry = i.next();
        BufKey key = entry.getKey();
        Annotation data = entry.getValue();
        Element e = metaModel.env.get(key.element);
        i.remove();
        metaModel.env.log("Broadcasting annotation " + key + " = " + data);
        for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
          plugin.processAnnotation(found, e, data);
        }
        found.processed.put(key, data);
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
