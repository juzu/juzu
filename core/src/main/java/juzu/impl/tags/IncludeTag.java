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

package juzu.impl.tags;

import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.ExtendedTagHandler;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.Template;
import juzu.impl.common.Path;
import juzu.template.Renderable;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IncludeTag extends ExtendedTagHandler {

  @Override
  public void compile(ProcessPhase phase, ASTNode.Tag tag, Template t) {
    String path = tag.getArgs().get("path");
    phase.resolveTemplate(Path.parse(path));
  }

  @Override
  public void render(TemplateRenderContext context, Renderable body, Map<String, String> args) throws IOException {
    String path = args.get("path");
    TemplateStub template = context.resolveTemplate(path);
    template.render(context);
  }
}
