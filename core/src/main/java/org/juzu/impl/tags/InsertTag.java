package org.juzu.impl.tags;

import org.juzu.template.Body;
import org.juzu.template.TemplateRenderContext;
import org.juzu.template.TagHandler;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InsertTag extends TagHandler
{

   @Override
   public void render(TemplateRenderContext context, Body body, Map<String, String> args) throws IOException
   {
      Body body_ = DecorateTag.current.get();
      if (body_ != null)
      {
         body_.render(context);
      }
   }
}
