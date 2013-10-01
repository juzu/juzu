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

import juzu.impl.common.Location;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.template.TagHandler;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessPhase extends CompilationPhase {


  /** . */
  private final ProcessContext context;

  public ProcessPhase(ProcessContext context) {
    super(context);

    //
    this.context = context;
  }

  public void process(Template<ASTNode.Template> template) throws TemplateException {
    doAttribute(template.getModel());
    doProcess(template, template.getModel());
    doResolve(template, template.getModel());
    doUnattribute(template.getModel());
  }

  public Template resolveTemplate(Path path) throws ProcessingException, TemplateException {
    return context.resolveTemplate(path);
  }

  private void doProcess(Template<ASTNode.Template> template, ASTNode<?> node) throws ProcessingException, TemplateException {
    if (node instanceof ASTNode.Template) {
      for (ASTNode.Block child : node.getChildren()) {
        doProcess(template, child);
      }
    }
    else if (node instanceof ASTNode.Section) {
      // Do nothing
    }
    else if (node instanceof ASTNode.URL) {
      ASTNode.URL urlNode = (ASTNode.URL)node;
      String typeName = urlNode.getTypeName();
      String methodName = urlNode.getMethodName();
      Map<String,String> parameters = urlNode.getArgs();
      MethodInvocation mi = context.resolveMethodInvocation(typeName, methodName, parameters);
      if (mi == null) {
        StringBuilder controller = new StringBuilder();
        if (typeName != null && typeName.length() > 0) {
          controller.append(typeName).append('.');
        }
        controller.append(methodName).append('(').append(parameters).append(')');
        Location location = urlNode.getBegin().getPosition();
        throw TemplateMetaModel.CONTROLLER_NOT_RESOLVED.failure(
            controller,
            template.getAbsolutePath().getCanonical(),
            location.getLine(),
            location.getCol());
      } else {
        urlNode.setInvocation(mi);
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

  private void doResolve(Template<ASTNode.Template> template, ASTNode<?> node) throws ProcessingException, TemplateException {
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
