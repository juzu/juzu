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
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.Key;
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
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class MetaModelProcessContext extends ProcessContext {

  /** . */
  private TemplatesMetaModel owner;

  /** . */
  private final ProcessingContext env;

  MetaModelProcessContext(TemplatesMetaModel owner) {
    this.owner = owner;
    this.env = owner.application.getProcessingContext();
  }

  void resolve(final TemplateMetaModel metaModel) {
    resolveTemplate(metaModel.getPath());
  }

  @Override
  protected <M extends Serializable> Template<M> getTemplate(Path.Relative path) {
    TemplateMetaModel tmm = owner.get(path);
    if (tmm != null) {
      return (Template<M>)tmm.template;
    } else {
      return null;
    }
  }

  @Override
  protected <M extends Serializable> void registerTemplate(Template<M> template) {
    TemplateMetaModel related = owner.add(template.getRelativePath());
    if (related.template != null) {
      throw new UnsupportedOperationException("todo");
    } else {
      related.template = template;
    }
  }

  @Override
  protected <M extends Serializable> void register(Path.Relative originPath, Template<M> template) {
    if (originPath != null) {
      TemplateMetaModel a = owner.get(template.getRelativePath());
      TemplateMetaModel b = owner.get(originPath);
      Key<TemplateMetaModel> key = Key.of(template.getAbsolutePath(), TemplateMetaModel.class);
      // It may already be here (in case of double include for instance)
      if (b.getChild(key) == null) {
        b.addChild(key, a);
      }
    }
  }

  @Override
  protected TemplateProvider resolverProvider(String ext) {
    return owner.plugin.providers.get(ext);
  }

  @Override
  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException {
    MethodMetaModel method = owner.getApplication().getChild(ControllersMetaModel.KEY).resolve(typeName, methodName, parameterMap.keySet());

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
  public Resource<Timestamped<Content>> resolveResource(Path.Relative path) {
    FileObject resource = owner.application.resolveResource(TemplatesMetaModel.LOCATION, path);
    if (resource != null) {
      try {
        Path.Absolute foo = owner.resolvePath(path);
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
