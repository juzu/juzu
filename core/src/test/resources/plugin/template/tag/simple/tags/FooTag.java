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

package plugin.template.tag.simple.tags;

import juzu.template.Renderable;
import juzu.template.TagHandler;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FooTag extends TagHandler {

  @Override
  public void render(TemplateRenderContext context, Renderable body, Map<String, String> args) throws IOException {
    context.getPrinter().write("<foo>");
    body.render(context);
    context.getPrinter().write("</foo>");
  }
}
