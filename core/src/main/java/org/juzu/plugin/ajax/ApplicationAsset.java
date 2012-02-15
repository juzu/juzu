package org.juzu.plugin.ajax;

import org.juzu.impl.asset.Route;
import org.juzu.impl.utils.Path;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationAsset extends Route implements Provider<ApplicationAsset>
{

   /** . */
   final ApplicationDescriptor desc;

   /** . */
   final Map<String, ControllerMethod> table;

   public ApplicationAsset(ApplicationDescriptor desc)
   {
      //
      Map<String, ControllerMethod> table = new HashMap<String, ControllerMethod>();
      for (ControllerMethod cm : desc.getControllerMethods())
      {
         Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
         if (ajax != null)
         {
            table.put(cm.getName(), cm);
         }
      }

      //
      this.desc = desc;
      this.table = table;
   }

   @Override
   public boolean serve(Path path, HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      String scope = desc.getName();

      resp.setContentType("text/javascript");
      PrintWriter printer = resp.getWriter();

      //
      printer.println("HTMLElement.prototype." + scope + "=function(){");
      printer.println("var capture=this;");
      printer.println("return {");

      //
      for (Map.Entry<String, ControllerMethod> entry : table.entrySet())
      {
         printer.println(entry.getKey() + ":function(){");
         printer.println("return capture.foo(\"" + entry.getValue().getId() + "\");");
         printer.println("}");
      }

      //
      printer.println("};");
      printer.println("};");

      //
      return true;
   }

   public ApplicationAsset get()
   {
      return this;
   }
}
