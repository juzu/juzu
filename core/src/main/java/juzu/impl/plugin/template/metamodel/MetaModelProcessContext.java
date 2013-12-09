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

import juzu.impl.common.Resource;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.template.spi.ParseContext;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.TemplateModel;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.template.TagHandler;

import javax.tools.FileObject;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class MetaModelProcessContext extends ProcessContext {

  /** . */
  private AbstractContainerMetaModel owner;

  /** . */
  private final ProcessingContext env;

  /** . */
  private final TemplateMetaModel metaModel;

  MetaModelProcessContext(AbstractContainerMetaModel owner, TemplateMetaModel metaModel) {
    this.owner = owner;
    this.env = owner.application.getProcessingContext();
    this.metaModel = metaModel;
  }

  void resolve(final TemplateMetaModel metaModel) throws TemplateException {
    resolveTemplate(metaModel.path);
  }

  @Override
  public TagHandler resolveTagHandler(String name) {
    return owner.resolveTagHandler(name);
  }

  @Override
  protected Path.Absolute resolvePath(Path.Relative path) {
    return owner.resolvePath(path);
  }

  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException {
    return owner.getApplication().resolveMethodInvocation(typeName, methodName, parameterMap);
  }

  @Override
  public Timestamped<Resource> resolveResource(Path.Absolute path) {
    FileObject resource = owner.application.resolveResource(path);
    if (resource != null) {
      try {
        byte[] bytes = Tools.bytes(resource.openInputStream());
        long lastModified = resource.getLastModified();
        return new Timestamped<Resource>(lastModified, new Resource(bytes, Charset.defaultCharset()));
      }
      catch (Exception e) {
        env.info("Could not get resource content " + path.getCanonical(), e);
      }
    }
    return null;
  }

  public Path.Absolute resolveTemplate(Path path) throws TemplateException {
    Path.Absolute absolute;
    if (path instanceof Path.Relative) {
      absolute = resolvePath((Path.Relative)path);
    } else {
      absolute = (Path.Absolute)path;
    }
    return resolveTemplate(absolute);
  }

  private <M extends Serializable> Path.Absolute resolveTemplate(Path.Absolute path) throws TemplateException {
    Template template;
    if (path.equals(metaModel.path)) {
      template = metaModel;
    } else {
      template = owner.add(path, Collections.<TemplateRefMetaModel>singletonList(this.metaModel));
    }
    if (template instanceof TemplateMetaModel) {
      TemplateMetaModel tmm = (TemplateMetaModel)template;
      if (tmm.templateModel == null) {
        Timestamped<Resource> resource = resolveResource(path);
        if (resource == null) {
          throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
        } else {
          TemplateProvider<M> provider = (TemplateProvider<M>)owner.resolveTemplateProvider(path.getExt());
          M templateAST;
          try {
            templateAST = provider.parse(new ParseContext(), resource.getObject().getCharSequence());
          }
          catch (TemplateException e1) {
            throw TemplateMetaModel.TEMPLATE_SYNTAX_ERROR.failure(path).initCause(e1);
          }
          TemplateModel<M> templateModel =  new TemplateModel<M>(
              templateAST,
              path,
              resource.getTime(),
              Tools.md5(resource.getObject().getBytes()));
          tmm.templateModel = templateModel;
          try {
            provider.process(new MetaModelProcessContext(owner, tmm), templateModel);
          }
          catch (TemplateException e) {
            throw TemplateMetaModel.TEMPLATE_VALIDATION_ERROR.failure(path);
          }
        }
      }
    }
    return path;
  }
}
