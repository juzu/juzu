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

import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.ExtendedTagHandler;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.Template;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.common.Path;
import juzu.template.Renderable;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DecorateTag extends ExtendedTagHandler {

  public DecorateTag() {
    super("decorate");
  }

  @Override
  public void process(ProcessPhase phase, ASTNode.Tag tag, Template t) {

    // Find the root template tag
    ASTNode current = tag;
    while (true) {
      if (current instanceof ASTNode.Block) {
        current = ((ASTNode.Block)current).getParent();
      }
      else {
        break;
      }
    }
    ASTNode.Template template = (ASTNode.Template)current;

    // Move the children of the template node as child of the decorate tag
    for (ASTNode.Block child : new ArrayList<ASTNode.Block<?>>(template.getChildren())) {
      if (child != tag) {
        tag.addChild(child);
      }
    }

    // Set decorate tag as unique child of the template node
    if (tag.getParent() != template) {
      template.addChild(tag);
    }
  }

  @Override
  public void compile(ProcessPhase phase, ASTNode.Tag tag, Template t) throws TemplateException {
    String path = tag.getArgs().get("path");
    Template resolved = phase.resolveTemplate(Path.parse(path));
    if (resolved == null) {
      throw TemplateMetaModel.TEMPLATE_NOT_RESOLVED.failure(path);
    } else {
      tag.getArgs().put("path", resolved.getPath().getCanonical());
    }
  }

  @Override
  public void render(TemplateRenderContext context, Renderable body, Map<String, String> args) throws IOException {
    InsertTag.current.get().addLast(body);
    try {
      String path = args.get("path");
      TemplateStub template = context.resolveTemplate(path);
      template.render(context);
    }
    finally {
      InsertTag.current.get().removeLast();
    }
  }
}
