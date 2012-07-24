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
    doEmit(new EmitterContext(generator), node.getChildren());
    doUnattribute(node);
  }

  private void doEmit(EmitterContext ctx, List<ASTNode.Block<?>> blocks) {
    for (ASTNode.Block block : blocks) {
      if (block instanceof ASTNode.Section) {
        ASTNode.Section section = (ASTNode.Section)block;
        ctx.begin(section.getType(), section.getBeginPosition());
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
        ctx.end();
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
      else {
        throw new AssertionError();
      }
    }
  }
}
