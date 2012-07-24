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
