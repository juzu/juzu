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

import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.juzu.DialectTemplateEmitter;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.common.Location;
import juzu.impl.common.MethodInvocation;
import juzu.template.TagHandler;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EmitPhase extends CompilationPhase {

  /** . */
  private final EmitContext context;

  public EmitPhase(EmitContext context) {
    this.context = context;
  }

  public void emit(DialectTemplateEmitter generator, ASTNode<?> node) {
    doAttribute(node);
    EmitterContext ctx = new EmitterContext(generator);
    ctx.open();
    doEmit(ctx, node.getChildren());
    ctx.close();
    doUnattribute(node);
  }

  private void doEmit(EmitterContext ctx, List<ASTNode.Block<?>> blocks) {
    for (ASTNode.Block block : blocks) {
      if (block instanceof ASTNode.Section) {
        ASTNode.Section section = (ASTNode.Section)block;
        ctx.openSection(section.getType(), section.getBeginPosition());
        int lineNumber = section.getBegin().getPosition().getLine();
        int colNumber = section.getBegin().getPosition().getCol();
        String text = section.getText();
        int from = 0;
        while (true) {
          int to = text.indexOf('\n', from);
          if (to != -1) {
            String chunk = text.substring(from, to);
            ctx.appendText(chunk);
            ctx.appendLineBreak(new Location(colNumber + (to - from), lineNumber));
            from = to + 1;
            lineNumber++;
            colNumber = 1;
          }
          else {
            String chunk = text.substring(from);
            ctx.appendText(chunk);
            break;
          }
        }
        ctx.closeSection();
      }
      else if (block instanceof ASTNode.URL) {
        ASTNode.URL url = (ASTNode.URL)block;
        MethodInvocation mi = url.getInvocation();
        ctx.writer.url(mi.getClassName(), mi.getMethodName(), mi.getMethodArguments());
      }
      else if (block instanceof ASTNode.Tag) {
        ASTNode.Tag tag = (ASTNode.Tag)block;
        TagHandler handler = get(tag);
        String className = handler.getClass().getName();
        if (tag.getChildren() != null) {
          ctx.writer.openTag(className, tag.getArgs());
          doEmit(ctx, tag.getChildren());
          ctx.writer.closeTag(className, tag.getArgs());
        }
        else {
          ctx.writer.tag(className, tag.getArgs());
        }
      }
      else if (block instanceof ASTNode.Message) {
        ASTNode.Message message = (ASTNode.Message)block;
        String key = message.getKey();
        ctx.writer.message(key);
      }
      else {
        throw new AssertionError("Cannot such block " + block);
      }
    }
  }
}
