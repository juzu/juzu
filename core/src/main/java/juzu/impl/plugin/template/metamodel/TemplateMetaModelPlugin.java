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

package juzu.impl.plugin.template.metamodel;

import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.common.FQN;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModelProcessor;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.Template;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final Pattern PATH_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

  /** . */
  private static final FQN PATH = new FQN(juzu.Path.class);

  /** . */
  Map<String, TemplateProvider> providers;

  public TemplateMetaModelPlugin() {
    super("plugin/template");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(juzu.Path.class);
  }

  @Override
  public void postActivate(ModuleMetaModel applications) {
    // Discover the template providers
    Iterable<TemplateProvider> loader = applications.env.loadServices(TemplateProvider.class);
    Map<String, TemplateProvider> providers = new HashMap<String, TemplateProvider>();
    for (TemplateProvider provider : loader) {
      providers.put(provider.getSourceExtension(), provider);
    }

    //
    this.providers = providers;
  }

  @Override
  public void init(ApplicationMetaModel application) {
    TemplatesMetaModel templates = new TemplatesMetaModel();
    templates.plugin = this;
    application.addChild(TemplatesMetaModel.KEY, templates);
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(PATH)) {
      if (key.getElement() instanceof ElementHandle.Field) {
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        Path removedPath = Path.parse((String)removed.get("value"));
        TemplatesMetaModel templates = metaModel.getChild(TemplatesMetaModel.KEY);
        metaModel.env.log("Removing template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + removedPath);
        templates.remove(variableElt);
      }
    }
  }

  @Override
  public void processAnnotationUpdated(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (key.getType().equals(PATH)) {
      if (key.getElement() instanceof ElementHandle.Field) {
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        Path addedPath = Path.parse((String)added.get("value"));
        Path removedPath = Path.parse((String)removed.get("value"));
        TemplatesMetaModel templates = metaModel.getChild(TemplatesMetaModel.KEY);
        metaModel.env.log("Updating template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + removedPath + "->" + addedPath);
        templates.remove(variableElt);
        templates.add(variableElt, addedPath);
      }
    }
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel application, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(PATH)) {
      if (key.getElement() instanceof ElementHandle.Field) {
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        TemplatesMetaModel templates = application.getChild(TemplatesMetaModel.KEY);
        Path addedPath = Path.parse((String)added.get("value"));
        application.env.log("Adding template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + addedPath);
        templates.add(variableElt, addedPath);
      }
      else if (key.getElement() instanceof ElementHandle.Class) {
        // We ignore it on purpose
      }
      else {
        throw MetaModelProcessor.ANNOTATION_UNSUPPORTED.failure(key);
      }
    }
  }

  @Override
  public void postActivate(ApplicationMetaModel application) {
    application.getChild(TemplatesMetaModel.KEY).plugin = this;
  }

  @Override
  public void prePassivate(ApplicationMetaModel application) {
    application.env.log("Passivating template resolver for " + application.getHandle());
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);
    metaModel.resolver.prePassivate();
    metaModel.plugin = null;
  }

  @Override
  public void prePassivate(ModuleMetaModel applications) {
    applications.env.log("Passivating templates");
    this.providers = null;
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {
    application.env.log("Processing templates of " + application.getHandle());
    application.getChild(TemplatesMetaModel.KEY).resolver.process(this, application.model.env);
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    JSON config = new JSON();
    ArrayList<String> list = new ArrayList<String>();
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);
    for (Template template : metaModel.resolver.getTemplates()) {
      Path resolved = metaModel.resolve(template.getPath());
      list.add(resolved.getFQN().getName());
    }
    config.map("templates", list);
    config.set("package", metaModel.getQN().toString());
    return config;
  }
}
