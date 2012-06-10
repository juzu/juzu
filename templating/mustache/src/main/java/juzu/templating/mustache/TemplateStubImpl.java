package juzu.templating.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import juzu.impl.template.spi.TemplateStub;
import juzu.io.Stream;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.io.StringWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateStubImpl extends TemplateStub {

  /** . */
  private Mustache mustache;

  @Override
  protected void doInit(ClassLoader loader) {
    String name = getClass().getSimpleName();
    String mustacheName = name.substring(0, name.length() - 1).replace('.', '/') + ".mustache";
    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(loader);
      DefaultMustacheFactory factory = new DefaultMustacheFactory(getClass().getPackage().getName().replace('.', '/')) {
        @Override
        public MustacheVisitor createMustacheVisitor() {
          return new DefaultMustacheVisitor(this) {
            @Override
            public void pragma(TemplateContext templateContext, String pragma, String args) {
              if ("param".equals(pragma)) {
                // Do nothing
              } else {
                super.pragma(templateContext, pragma, args);
              }
            }
          };
        }
      };
      mustache = factory.compile(mustacheName);
    }
    finally {
      Thread.currentThread().setContextClassLoader(previous);
    }
  }

  @Override
  protected void doRender(TemplateRenderContext renderContext) throws TemplateExecutionException, IOException {
    StringWriter buffer = new StringWriter();
    mustache.execute(buffer, new Object[]{new ContextMap(renderContext)});
    Stream.Char stream = renderContext.getPrinter();
    stream.append(buffer.getBuffer());
  }
}
