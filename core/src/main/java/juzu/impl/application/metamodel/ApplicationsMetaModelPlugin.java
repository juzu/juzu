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
import juzu.impl.compiler.AnnotationData;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.common.JSON;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationsMetaModelPlugin extends MetaModelPlugin {

  /** . */
  private HashSet<Class<? extends Annotation>> annotationTypes;

  public ApplicationsMetaModelPlugin() {
    super("applications");
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return annotationTypes;
  }

  @Override
  public JSON toJSON(MetaModel metaModel) {
    return metaModel.getChild(ApplicationsMetaModel.KEY).toJSON();
  }

  @Override
  public void init(MetaModel metaModel) {
    // Discover plugins
    LinkedHashMap<String, ApplicationMetaModelPlugin> plugins = new LinkedHashMap<String, ApplicationMetaModelPlugin>();
    StringBuilder msg = new StringBuilder("Using application plugins:");
    for (ApplicationMetaModelPlugin plugin : metaModel.env.loadServices(ApplicationMetaModelPlugin.class)) {
      msg.append(" ").append(plugin.getName());
      plugins.put(plugin.getName(), plugin);
    }
    MetaModel.log.log(msg);

    // We are interested by the Application annotation
    HashSet<Class<? extends Annotation>> annotationTypes = new HashSet<Class<? extends Annotation>>();
    annotationTypes.add(Application.class);

    // Add the plugin annotations
    for (ApplicationMetaModelPlugin plugin : plugins.values()) {
      Set<Class<? extends Annotation>> processed = plugin.getAnnotationTypes();
      MetaModel.log.log("Application plugin " + plugin.getName() + " wants to process " + processed);
      annotationTypes.addAll(processed);
    }

    // Create application
    ApplicationsMetaModel application = new ApplicationsMetaModel();

    // Add the applications container to the meta model
    metaModel.addChild(ApplicationsMetaModel.KEY, application);

    // Add plugins
    for (Map.Entry<String, ApplicationMetaModelPlugin> entry : plugins.entrySet()) {
      application.addPlugin(entry.getKey(), entry.getValue());
    }

    //
    this.annotationTypes = annotationTypes;
  }

  @Override
  public void postActivate(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postActivate(metaModel);
  }

  @Override
  public void processAnnotation(MetaModel metaModel, Element element, String fqn, AnnotationData data) {
    metaModel.getChild(ApplicationsMetaModel.KEY).processAnnotation(metaModel, element, fqn, data);
  }

  @Override
  public void postProcessAnnotations(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postProcessAnnotations(metaModel);
  }

  @Override
  public void processEvents(MetaModel metaModel, EventQueue queue) {
    metaModel.getChild(ApplicationsMetaModel.KEY).processEvents(metaModel, queue);
  }

  @Override
  public void postProcessEvents(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postProcessEvents(metaModel);
  }

  @Override
  public void prePassivate(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).prePassivate(metaModel);
  }
}
