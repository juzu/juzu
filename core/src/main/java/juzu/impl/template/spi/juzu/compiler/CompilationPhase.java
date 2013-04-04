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
import juzu.impl.tags.DecorateTag;
import juzu.impl.tags.IncludeTag;
import juzu.impl.tags.InsertTag;
import juzu.impl.tags.ParamTag;
import juzu.impl.tags.TitleTag;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.template.TagHandler;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationPhase {

  /** . */
  private final Map<String, TagHandler> tags = new HashMap<String, TagHandler>();

  /** . */
  private final IdentityHashMap<ASTNode.Tag, TagHandler> tagHandlers = new IdentityHashMap<ASTNode.Tag, TagHandler>();

  public CompilationPhase() {
    // Built in tags

    tags.put("include", new IncludeTag());
    tags.put("insert", new InsertTag());
    tags.put("decorate", new DecorateTag());
    tags.put("title", new TitleTag());
    tags.put("param", new ParamTag());
  }

  public TagHandler resolveTag(String name) {
    return tags.get(name);
  }

  public TagHandler get(ASTNode.Tag node) {
    return tagHandlers.get(node);
  }

  protected void doAttribute(ASTNode<?> node) throws ProcessingException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doAttribute(child);
      }
    }
    else if (node instanceof ASTNode.Section) {
      // Do nothing
    }
    else if (node instanceof ASTNode.URL) {
      // Do nothing
    }
    else if (node instanceof ASTNode.Tag) {
      ASTNode.Tag nodeTag = (ASTNode.Tag)node;
      TagHandler handler = resolveTag(nodeTag.getName());
      if (handler == null) {
        throw new UnsupportedOperationException("handle me gracefully " + nodeTag.getName());
      }
      tagHandlers.put(nodeTag, handler);
      for (ASTNode.Block<?> child : nodeTag.getChildren()) {
        doAttribute(child);
      }
    }
  }

  protected void doUnattribute(ASTNode<?> node) throws ProcessingException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doUnattribute(child);
      }
    }
    else if (node instanceof ASTNode.Section) {
      // Do nothing
    }
    else if (node instanceof ASTNode.URL) {
      // Do nothing
    }
    else if (node instanceof ASTNode.Tag) {
      ASTNode.Tag nodeTag = (ASTNode.Tag)node;
      tagHandlers.remove(nodeTag);
      for (ASTNode.Block<?> child : nodeTag.getChildren()) {
        doAttribute(child);
      }
    }
  }
}
