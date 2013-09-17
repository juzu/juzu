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

package juzu.impl.plugin.template.metamodel;

import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.ParameterMetaModel;
import juzu.impl.plugin.controller.metamodel.PhaseParameterMetaModel;
import juzu.impl.common.Resource;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.common.Content;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;

import javax.tools.FileObject;
import java.nio.charset.Charset;
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
      return null;
    }

    //
    List<String> args = new ArrayList<String>();
    for (ParameterMetaModel param : method.getParameters()) {
      if (param instanceof PhaseParameterMetaModel) {
        String value = parameterMap.get(param.getName());
        args.add(value);
      }
    }
    return new MethodInvocation(method.getController().getHandle().getFQN() + "_", method.getName(), args);
  }

  @Override
  protected Resource<Timestamped<Content>> resolveResource(Path.Relative path) {
    TemplatesMetaModel tmm = templateMetaModel.getTemplates();
    FileObject resource = tmm.application.resolveResource(TemplatesMetaModel.LOCATION, path);
    if (resource != null) {
      try {
        Path.Absolute foo = templateMetaModel.getTemplates().resolvePath(path);
        byte[] bytes = Tools.bytes(resource.openInputStream());
        long lastModified = resource.getLastModified();
        Timestamped<Content> content = new Timestamped<Content>(lastModified, new Content(bytes, Charset.defaultCharset()));
        return new Resource<Timestamped<Content>>(foo, content);
      }
      catch (Exception e) {
        env.log("Could not get resource content " + path.getCanonical(), e);
      }
    }
    return null;
  }
}
