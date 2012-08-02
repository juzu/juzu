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

import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.ParameterMetaModel;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.common.Content;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ModelTemplateProcessContext extends ProcessContext {

  /** . */
  private TemplateMetaModel templateMetaModel;

  /** . */
  private final ProcessingContext env;

  ModelTemplateProcessContext(
    TemplateMetaModel templateMetaModel,
    Map<Path, Template<?>> templates,
    ProcessingContext env) {
    super(templates);
    this.templateMetaModel = templateMetaModel;
    this.env = env;
  }

  Collection<Template<?>> resolve(final TemplateMetaModel metaModel) {
    ElementHandle.Field handle = templateMetaModel.getRefs().iterator().next().getHandle();
    return env.executeWithin(handle, new Callable<Collection<Template<?>>>() {
      public Collection<Template<?>> call() throws Exception {
        Set<Path> keys = new HashSet<Path>(templates.keySet());
        resolveTemplate(metaModel.getPath());
        Map<Path, Template<?>> copy = new HashMap<Path, Template<?>>(templates);
        copy.keySet().removeAll(keys);
        return copy.values();
      }
    });
  }

  @Override
  protected TemplateProvider resolverProvider(String ext) {
    return templateMetaModel.getTemplates().plugin.providers.get(ext);
  }

  @Override
  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException {
    MethodMetaModel method = templateMetaModel.getTemplates().getApplication().getChild(ControllersMetaModel.KEY).resolve(typeName, methodName, parameterMap.keySet());

    //
    if (method == null) {
      throw ControllerMetaModel.CONTROLLER_METHOD_NOT_RESOLVED.failure(methodName + "(" + parameterMap + ")");
    }

    //
    List<String> args = new ArrayList<String>();
    for (ParameterMetaModel param : method.getParameters()) {
      String value = parameterMap.get(param.getName());
      args.add(value);
    }
    return new MethodInvocation(method.getController().getHandle().getFQN().getName() + "_", method.getName() + "URL", args);
  }

  protected Content resolveResource(Path path) {
    TemplatesMetaModel tmm = templateMetaModel.getTemplates();
    ElementHandle.Package context = tmm.getApplication().getHandle();
    return env.resolveResource(context, tmm.resolve(path));
  }
}
