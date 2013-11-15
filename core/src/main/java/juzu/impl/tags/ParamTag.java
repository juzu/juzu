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

package juzu.impl.tags;

import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.ExtendedTagHandler;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.TemplateModel;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ParamTag extends ExtendedTagHandler {

  public ParamTag() {
    super("param");
  }

  @Override
  public void process(ProcessPhase phase, ASTNode.Tag tag, TemplateModel t) {
    String parameterName = tag.getArgs().get("name");
    t.addParameter(parameterName);
  }
}
