package template.tag.simple.tags;

import org.juzu.template.Body;
import org.juzu.template.TemplateRenderContext;
import org.juzu.template.TagHandler;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FooTag extends TagHandler
{

   @Override
   public void render(TemplateRenderContext context, Body body, Map<String, String> args) throws IOException
   {
      context.getPrinter().write("<foo>");
      body.render(context);
      context.getPrinter().write("</foo>");
   }
}
