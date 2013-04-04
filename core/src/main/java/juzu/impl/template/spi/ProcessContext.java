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

import juzu.impl.common.Timestamped;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.common.Content;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;

import java.io.Serializable;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessContext {

  /** . */
  protected final Map<Path, Template<?>> templates;

  public ProcessContext(Map<Path, Template<?>> templates) {
    this.templates = templates;
  }

  protected Path.Absolute resolvePath(Path.Relative path) {
    return Path.absolute(path.getName(), path.getExt());
  }

  protected Timestamped<Content> resolveResource(Path.Absolute path) {
    return null;
  }

  protected TemplateProvider resolverProvider(String ext) {
    return null;
  }

  public Template resolveTemplate(Path.Relative path) {
    return resolveTemplate(path, path);
  }

  public <M extends Serializable> Template<? extends M> resolveTemplate(
      Path.Relative originPath,
      Path.Relative path) {

    // A class cast here would mean a terrible issue
    Template<M> template = (Template<M>)templates.get(path);

    //
    if (template == null) {

      // Resolve
      Path.Absolute resolved = resolvePath(path);

      // Get source
      Timestamped<Content> content = resolveResource(resolved);
      if (content == null) {
        throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
      }

      //
      TemplateProvider<M> provider = (TemplateProvider<M>)resolverProvider(path.getExt());

      // Parse to AST
      M templateAST;
      try {
        templateAST = provider.parse(new ParseContext(), content.getObject().getCharSequence());
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_SYNTAX_ERROR.failure(path);
      }

      // Add template to application
      template =  new Template<M>(
        originPath,
        templateAST,
        path,
        resolved,
        content.getTime());

      //
      templates.put(path, template);

      // Process template
      try {
        provider.process(this, template);
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_VALIDATION_ERROR.failure(path);
      }
    }

    //
    return template;
  }

  public MethodInvocation resolveMethodInvocation(
    String typeName,
    String methodName,
    Map<String, String> parameterMap) throws ProcessingException {
    return null;
  }
}
