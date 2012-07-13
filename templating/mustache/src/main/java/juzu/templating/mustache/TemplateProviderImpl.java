package juzu.templating.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.ParseContext;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.common.Path;

import java.io.Reader;
import java.io.StringReader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateProviderImpl extends TemplateProvider<MustacheContext> {

  @Override
  public Class<? extends TemplateStub> getTemplateStubType() {
    return TemplateStubImpl.class;
  }

  @Override
  public MustacheContext parse(ParseContext context, CharSequence source) {
    return new MustacheContext(source.toString());
  }

  @Override
  public void process(final ProcessContext context, final Template<MustacheContext> mustacheTemplate) {
    // Nothing to do for now

    // Visit the mustache
    MustacheFactory factory = new DefaultMustacheFactory() {

      @Override
      public Reader getReader(String resourceName) {
        Path partialPath = Path.parse(resourceName);
        Template<MustacheContext> partial = (Template<MustacheContext>)context.resolveTemplate(mustacheTemplate.getOriginPath(), partialPath);
        if (partial != null) {
          return new StringReader(partial.getModel().source);
        } else {
          return null;
        }
      }

      public MustacheVisitor createMustacheVisitor() {
        return new DefaultMustacheVisitor(this) {
          @Override
          public void pragma(TemplateContext templateContext, String pragma, String args) {
            if ("param".equals(pragma)) {
              mustacheTemplate.addParameter(args);
            } else {
              super.pragma(templateContext, pragma, args);
            }
          }
        };
      }
    };

    // Does the name count ?
    factory.compile(new StringReader(mustacheTemplate.getModel().source), mustacheTemplate.getPath().getName());
  }

  @Override
  public CharSequence emit(EmitContext context, MustacheContext templateModel) {
    return null;
  }

  @Override
  public String getSourceExtension() {
    return "mustache";
  }

  @Override
  public String getTargetExtension() {
    return null;
  }
}
