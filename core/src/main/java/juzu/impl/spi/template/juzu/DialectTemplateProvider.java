package juzu.impl.spi.template.juzu;

import juzu.impl.spi.template.EmitContext;
import juzu.impl.spi.template.ProcessContext;
import juzu.impl.spi.template.Template;
import juzu.impl.spi.template.TemplateException;
import juzu.impl.spi.template.TemplateProvider;
import juzu.impl.spi.template.juzu.ast.ASTNode;
import juzu.impl.spi.template.juzu.ast.ParseException;
import juzu.impl.spi.template.juzu.compiler.EmitPhase;
import juzu.impl.spi.template.juzu.compiler.ProcessPhase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class DialectTemplateProvider extends TemplateProvider<ASTNode.Template> {

  protected abstract DialectTemplateEmitter createEmitter();

  @Override
  public final ASTNode.Template parse(CharSequence s) throws TemplateException {
    try {
      return ASTNode.Template.parse(s);
    }
    catch (ParseException e) {
      throw new TemplateException(e);
    }
  }

  @Override
  public final CharSequence emit(EmitContext context, ASTNode.Template ast) {
    DialectTemplateEmitter emitter = createEmitter();
    EmitPhase tcc = new EmitPhase(context);
    tcc.emit(emitter, ast);
    return emitter.toString();
  }

  @Override
  public final void process(ProcessContext context, Template template) {
    new ProcessPhase(context).process(template);
  }
}
