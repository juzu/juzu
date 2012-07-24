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
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.template.TagHandler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessPhase extends CompilationPhase {


  /** . */
  private final ProcessContext context;

  public ProcessPhase(ProcessContext context) {
    this.context = context;
  }

  /** . */
  private Path originPath;

  public void process(Template<ASTNode.Template> template) {
    boolean initial;
    if (originPath == null) {
      originPath = template.getPath();
      initial = true;
    }
    else {
      initial = false;
    }

    try {
      doAttribute(template.getModel());
      doProcess(template, template.getModel());
      doResolve(template, template.getModel());
      doUnattribute(template.getModel());
    }
    finally {
      if (initial) {
        originPath = null;
      }
    }
  }

  public Template resolveTemplate(Path path) throws ProcessingException {
    return context.resolveTemplate(originPath, path);
  }

  private void doProcess(Template<ASTNode.Template> template, ASTNode<?> node) throws ProcessingException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doProcess(template, child);
      }
    }
    else if (node instanceof ASTNode.Section) {
      // Do nothing
    }
    else if (node instanceof ASTNode.URL) {
      ASTNode.URL url = (ASTNode.URL)node;
      MethodInvocation mi = context.resolveMethodInvocation(url.getTypeName(), url.getMethodName(), url.getArgs());
      if (mi == null) {
        throw new UnsupportedOperationException("handle me gracefully");
      } else {
        url.setInvocation(mi);
      }
    }
    else if (node instanceof ASTNode.Tag) {
      ASTNode.Tag nodeTag = (ASTNode.Tag)node;
      TagHandler handler = get(nodeTag);
      if (handler instanceof ExtendedTagHandler) {
        ((ExtendedTagHandler)handler).process(this, nodeTag, template);
      }
      for (ASTNode.Block child : nodeTag.getChildren()) {
        doProcess(template, child);
      }
    }
  }

  private void doResolve(Template<ASTNode.Template> template, ASTNode<?> node) throws ProcessingException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doResolve(template, child);
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
      TagHandler handler = get(nodeTag);
      if (handler instanceof ExtendedTagHandler) {
        ((ExtendedTagHandler)handler).compile(this, nodeTag, template);
      }
      for (ASTNode.Block child : nodeTag.getChildren()) {
        doResolve(template, child);
      }
    }
  }
}
