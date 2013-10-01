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
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.impl.template.spi.juzu.PhaseContext;

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
  public abstract Resource<Timestamped<Content>> resolveResource(Path.Relative path);

  public abstract MethodInvocation resolveMethodInvocation( String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException;

  protected abstract TemplateProvider resolverProvider(String ext);

  protected abstract <M extends Serializable> Template<M> getTemplate(Path.Relative path);

  protected abstract <M extends Serializable> void registerTemplate(Template<M> template);

  protected abstract <M extends Serializable> void register(Path.Relative originPath, Template<M> template);


  public Template resolveTemplate(Path.Relative path) {
    return resolveTemplate(null, path);
  }

  public <M extends Serializable> Template<? extends M> resolveTemplate(
      Path.Relative originPath,
      Path.Relative path) {

    // A class cast here would mean a terrible issue
    Template<M> template = getTemplate(path);

    //
    if (template == null) {

      // Get source
      Resource<Timestamped<Content>> resolved = resolveResource(path);
      if (resolved == null) {
        throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
      }

      //
      TemplateProvider<M> provider = (TemplateProvider<M>)resolverProvider(path.getExt());

      // Parse to AST
      M templateAST;
      try {
        templateAST = provider.parse(new ParseContext(), resolved.content.getObject().getCharSequence());
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_SYNTAX_ERROR.failure(path);
      }

      // Add template to application
      template =  new Template<M>(
        templateAST,
        path,
        resolved.path,
        resolved.content.getTime());

      // Register template
      registerTemplate(template);

      // Process template
      try {
        provider.process(this, template);
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_VALIDATION_ERROR.failure(path);
      }
    }

    // Register relationship
    register(originPath, template);

    //
    return template;
  }
}
