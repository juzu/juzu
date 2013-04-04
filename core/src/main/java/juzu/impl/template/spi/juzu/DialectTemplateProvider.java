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

package juzu.impl.template.spi.juzu;

import juzu.impl.template.spi.ParseContext;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.ast.ParseException;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class DialectTemplateProvider extends TemplateProvider<ASTNode.Template> {

  @Override
  public final ASTNode.Template parse(ParseContext context, CharSequence source) throws TemplateException {
    try {
      return ASTNode.Template.parse(source);
    }
    catch (ParseException e) {
      throw new TemplateException(e);
    }
  }

  @Override
  public final void process(ProcessContext context, Template<ASTNode.Template> template) {
    new ProcessPhase(context).process(template);
  }
}
