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

package juzu.impl.template.spi.juzu.compiler;

import juzu.impl.compiler.ProcessingException;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.template.TagHandler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ExtendedTagHandler extends TagHandler {

  protected ExtendedTagHandler(String name) {
    super(name);
  }

  public void process(ProcessPhase phase, ASTNode.Tag tag, Template t) throws ProcessingException, TemplateException {
  }

  public void compile(ProcessPhase phase, ASTNode.Tag tag, Template t) throws ProcessingException, TemplateException {
  }
}
