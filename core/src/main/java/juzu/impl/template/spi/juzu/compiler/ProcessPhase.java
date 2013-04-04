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
  private Path.Relative originPath;

  public void process(Template<ASTNode.Template> template) {
    boolean initial;
    if (originPath == null) {
      originPath = template.getRelativePath();
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

  public Template resolveTemplate(Path.Relative path) throws ProcessingException {
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
