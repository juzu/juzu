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

import juzu.impl.common.CycleDetectionException;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.ParameterMetaModel;
import juzu.impl.plugin.controller.metamodel.PhaseParameterMetaModel;
import juzu.impl.common.Resource;
import juzu.impl.template.spi.ParseContext;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.common.Content;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.template.TagHandler;

import javax.tools.FileObject;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class MetaModelProcessContext extends ProcessContext {

  /** . */
  private AbstractContainerMetaModel owner;

  /** . */
  private final ProcessingContext env;

  /** . */
  private final Collection<? extends TemplateRefMetaModel> refs;

  MetaModelProcessContext(AbstractContainerMetaModel owner, Collection<? extends TemplateRefMetaModel> refs) {
    this.refs = refs;
    this.owner = owner;
    this.env = owner.application.getProcessingContext();
  }

  void resolve(final TemplateMetaModel metaModel) throws TemplateException {
    Path.Absolute abs = owner.getQN().resolve(metaModel.path);
    resolveTemplate(abs);
  }

  @Override
  public TagHandler resolveTagHandler(String name) {
    return owner.resolveTagHandler(name);
  }

  @Override
  protected Path.Absolute resolvePath(Path.Relative path) {
    return owner.resolvePath(path);
  }

  @Override
  protected <M extends Serializable> Template<M> getTemplate(Path.Absolute path) {
    TemplateMetaModel tmm = owner.get(path);
    if (tmm != null) {
      return (Template<M>)tmm.template;
    } else {
      return null;
    }
  }

  @Override
  protected <M extends Serializable> M parseTemplate(TemplateProvider<M> provider, Path.Absolute path, CharSequence s) throws TemplateException {
    try {
      return provider.parse(new ParseContext(), s);
    }
    catch (TemplateException e) {
      throw TemplateMetaModel.TEMPLATE_SYNTAX_ERROR.failure(path);
    }
  }

  @Override
  protected <M extends Serializable> void processTemplate(TemplateProvider<M> provider, Template<M> template) throws TemplateException {
    Path.Absolute path = template.getPath();
    if (owner.getQN().isPrefix(path.getName())) {
      TemplateMetaModel metaModel;
      if (!refs.isEmpty()) {
        if (owner.templates.get(path) != null) {
          throw new AssertionError();
        } else {
          for (TemplateRefMetaModel ref : refs) {
            owner.add(path, ref);
          }
        }
      }
      if ((metaModel = owner.templates.get(path)) == null) {
        throw new AssertionError();
      }
      metaModel.template = template;
      try {
        provider.process(new MetaModelProcessContext(owner, Collections.singletonList(metaModel)), template);
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_VALIDATION_ERROR.failure(path);
      }
    } else {
      throw new AssertionError("Should not happen");
    }
  }

  @Override
  protected <M extends Serializable> void linkTemplate(Template<M> template) {
    TemplateMetaModel a = owner.get(template.getPath());
    for (TemplateRefMetaModel ref : refs) {
      try {
        ref.add(a);
      }
      catch (CycleDetectionException e) {
        // We have a template cycle and we want to prevent it
        StringBuilder path = new StringBuilder();
        for (Object node : e.getPath()) {
          if (path.length() > 0) {
            path.append("->");
          }
          if (node instanceof TemplateMetaModel) {
            TemplateMetaModel templateNode = (TemplateMetaModel)node;
            path.append(templateNode.getPath().getValue());
          } else {
            // WTF ?
            path.append(node);
          }
        }
        throw TemplateMetaModel.TEMPLATE_CYCLE.failure(template.getPath(), path);
      }
    }
  }

  @Override
  protected TemplateProvider resolverProvider(String ext) {
    return owner.resolveTemplateProvider(ext);
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
    return new MethodInvocation(method.getController().getHandle().getName() + "_", method.getName(), args);
  }

  @Override
  public Resource<Timestamped<Content>> resolveResource(Path.Absolute path) {
    FileObject resource = owner.application.resolveResource(path);
    if (resource != null) {
      try {
        byte[] bytes = Tools.bytes(resource.openInputStream());
        long lastModified = resource.getLastModified();
        Timestamped<Content> content = new Timestamped<Content>(lastModified, new Content(bytes, Charset.defaultCharset()));
        return new Resource<Timestamped<Content>>(path, content);
      }
      catch (Exception e) {
        env.log("Could not get resource content " + path.getCanonical(), e);
      }
    }
    return null;
  }
}
