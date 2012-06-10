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
