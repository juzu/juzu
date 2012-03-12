package org.juzu.impl.plugin.ajax;

import org.juzu.impl.asset.Route;
import org.juzu.impl.utils.Path;
import org.juzu.impl.utils.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PluginAsset extends Route
{

   /** . */
   private static final String scriptJS;

   static
   {
      try
      {
         scriptJS = Tools.read(AjaxLifeCycle.class.getResource("script.js"));
      }
      catch (IOException e)
      {
         throw new Error("Cannot log plugin javascript", e);
      }
   }

   @Override
   public boolean serve(Path path, HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      resp.setContentType("text/javascript");
      PrintWriter writer = resp.getWriter();
      writer.print(scriptJS);
      return true;
   }
}
