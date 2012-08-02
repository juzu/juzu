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

package juzu.impl.template.spi;

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

  protected Content resolveResource(Path path) {
    return null;
  }

  protected TemplateProvider resolverProvider(String ext) {
    return null;
  }

  public Template resolveTemplate(Path path) {
    return resolveTemplate(path, path);
  }

  public <M extends Serializable> Template<? extends M> resolveTemplate(Path originPath, Path path) {

    // A class cast here would mean a terrible issue
    Template<M> template = (Template<M>)templates.get(path);

    //
    if (template == null) {
      // Get source
      Content content = resolveResource(path);
      if (content == null) {
        throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
      }

      //
      TemplateProvider<M> provider = (TemplateProvider<M>)resolverProvider(path.getExt());

      // Parse to AST
      M templateAST;
      try {
        templateAST = provider.parse(new ParseContext(), content.getCharSequence());
      }
      catch (TemplateException e) {
        throw TemplateMetaModel.TEMPLATE_SYNTAX_ERROR.failure(path);
      }

      // Add template to application
      template =  new Template<M>(
        originPath,
        templateAST,
        path,
        content.getLastModified());

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
