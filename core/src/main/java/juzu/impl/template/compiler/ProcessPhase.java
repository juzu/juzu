/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.template.compiler;

import juzu.impl.compiler.CompilationException;
import juzu.impl.template.ast.ASTNode;
import juzu.impl.utils.Path;
import juzu.template.TagHandler;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessPhase extends CompilationPhase {

  /** . */
  private final Map<Path, Template> templates;

  /** . */
  private final ProcessContext context;

  public ProcessPhase(ProcessContext context, Map<Path, Template> templates) {
    this.templates = templates;
    this.context = context;
  }

  public Map<Path, Template> getTemplates() {
    return templates;
  }

  /** . */
  private Path originPath;

  public Template resolveTemplate(Path path) throws CompilationException {
    boolean initial;
    if (originPath == null) {
      originPath = path;
      initial = true;
    }
    else {
      initial = false;
    }

    //
    try {
      Template template = templates.get(path);

      //
      if (template == null) {
        template = context.resolveTemplate(originPath, path);

        //
        if (template != null) {
          templates.put(path, template);

          //
          ASTNode.Template templateAST = template.getAST();

          // Process template
          doAttribute(templateAST);
          doProcess(template, templateAST);
          doResolve(template, templateAST);
          doUnattribute(templateAST);
        }
      }

      //
      return template;
    }
    finally {
      if (initial) {
        originPath = null;
      }
    }
  }

  private void doProcess(Template template, ASTNode<?> node) throws CompilationException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doProcess(template, child);
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
        ((ExtendedTagHandler)handler).process(this, nodeTag, template);
      }
      for (ASTNode.Block child : nodeTag.getChildren()) {
        doProcess(template, child);
      }
    }
  }

  private void doResolve(Template template, ASTNode<?> node) throws CompilationException {
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
