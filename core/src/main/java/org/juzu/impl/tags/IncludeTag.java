package org.juzu.impl.tags;

import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.template.ExtendedTagHandler;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.template.Body;
import org.juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class IncludeTag extends ExtendedTagHandler
{

   @Override
   public void compile(TemplateCompilationContext context, Map<String, String> args) throws IOException
   {
      String path = args.get("path");
      context.resolveTemplate(path);
   }

   @Override
   public void render(TemplateRenderContext context, Body body, Map<String, String> args) throws IOException
   {
      String path = args.get("path");
      TemplateStub template = context.resolveTemplate(path);
      template.render(context);
   }
}
