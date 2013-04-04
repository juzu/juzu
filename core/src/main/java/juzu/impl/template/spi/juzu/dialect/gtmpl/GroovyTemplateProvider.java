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

package juzu.impl.template.spi.juzu.dialect.gtmpl;

import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.DialectTemplateProvider;
import juzu.impl.template.spi.juzu.ast.ASTNode;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyTemplateProvider extends DialectTemplateProvider {

  @Override
  public Class<? extends TemplateStub> getTemplateStubType() {
    return GroovyTemplateStub.class;
  }

  @Override
  public String getSourceExtension() {
    return "gtmpl";
  }

  @Override
  public final void emit(EmitContext context, Template<ASTNode.Template> template) throws TemplateException, IOException {
    GroovyTemplateEmitter emitter = new GroovyTemplateEmitter(template.getAbsolutePath().getName());
    EmitPhase tcc = new EmitPhase(context);
    tcc.emit(emitter, template.getModel());
    context.createResource(template.getRelativePath().getRawName() + "_", "groovy", emitter.toString());
  }
}
