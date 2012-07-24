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

package juzu.impl.template.spi.juzu;

import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.ParseContext;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.ast.ParseException;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class DialectTemplateProvider extends TemplateProvider<ASTNode.Template> {

  protected abstract DialectTemplateEmitter createEmitter();

  @Override
  public final ASTNode.Template parse(ParseContext context, CharSequence source) throws TemplateException {
    try {
      return ASTNode.Template.parse(source);
    }
    catch (ParseException e) {
      throw new TemplateException(e);
    }
  }

  @Override
  public final CharSequence emit(EmitContext context, ASTNode.Template templateModel) {
    DialectTemplateEmitter emitter = createEmitter();
    EmitPhase tcc = new EmitPhase(context);
    tcc.emit(emitter, templateModel);
    return emitter.toString();
  }

  @Override
  public final void process(ProcessContext context, Template template) {
    new ProcessPhase(context).process(template);
  }
}
