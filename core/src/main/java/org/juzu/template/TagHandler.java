package org.juzu.template;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class TagHandler
{
   public void render(TemplateRenderContext context, Body body, Map<String, String> args) throws IOException
   {
      body.render(context);
   }
}
