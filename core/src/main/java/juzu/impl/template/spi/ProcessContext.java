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

package juzu.impl.template.spi;

import juzu.impl.common.Resource;
import juzu.impl.common.Content;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;

import java.io.Serializable;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ProcessContext extends PhaseContext {

  /**
   * Resolve a resource for the provided relative path.
   *
   * @param path the resource path
   * @return the resource or null if the resource could not be resolved
   */
  public abstract Resource<Timestamped<Content>> resolveResource(Path.Absolute path);

  /**
   * Resolve a resource for the provided relative path.
   *
   * @param path the resource path
   * @return the resource or null if the resource could not be resolved
   */
  public Resource<Timestamped<Content>> resolveResource(Path path) {
    Path.Absolute abs;
    if (path instanceof Path.Absolute) {
      abs = (Path.Absolute)path;
    } else {
      abs = resolvePath((Path.Relative)path);
    }
    return resolveResource(abs);
  }

  public abstract MethodInvocation resolveMethodInvocation( String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException;

  protected abstract TemplateProvider resolverProvider(String ext);

  protected abstract <M extends Serializable> Template<M> getTemplate(Path.Absolute path);

  protected abstract <M extends Serializable> void linkTemplate(Template<M> template);

  protected abstract Path.Absolute resolvePath(Path.Relative path);

  protected <M extends Serializable> void processTemplate(TemplateProvider<M> provider, Template<M> template) throws TemplateException {
    provider.process(this, template);
  }

  protected <M extends Serializable> M parseTemplate(TemplateProvider<M> provider, Path.Absolute path, CharSequence s) throws TemplateException {
    return provider.parse(new ParseContext(), s);
  }

  public <M extends Serializable> Template resolveTemplate(Path path) throws TemplateException {
    Path.Absolute abs;
    if (path instanceof Path.Relative) {
      abs = resolvePath((Path.Relative)path);
    } else {
      abs = (Path.Absolute)path;
    }
    Template<M> template = getTemplate(abs);
    if (template == null) {
      Resource<Timestamped<Content>> resolved = resolveResource(abs);
      if (resolved == null) {
        throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
      } else {
        TemplateProvider<M> provider = (TemplateProvider<M>)resolverProvider(path.getExt());
        M templateAST = parseTemplate(provider, abs, resolved.content.getObject().getCharSequence());
        template =  new Template<M>(
            templateAST,
            resolved.path,
            resolved.content.getTime(),
            Tools.md5(resolved.content.getObject().getBytes()));
        processTemplate(provider, template);
      }
    } else {
      linkTemplate(template);
    }
    return template;
  }
}
