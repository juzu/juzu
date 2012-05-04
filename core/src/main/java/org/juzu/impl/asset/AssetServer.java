package org.juzu.impl.asset;

import org.juzu.impl.application.ApplicationRuntime;
import org.juzu.impl.utils.NameLiteral;
import org.juzu.impl.utils.Tools;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServer
{

   /** . */
   public static final Named PLUGIN = new NameLiteral("plugin");

   /** . */
   public static final Named APPLICATION = new NameLiteral("application");

   /** . */
   HashSet<ApplicationRuntime<?, ?, ?>> runtimes = new HashSet<ApplicationRuntime<?, ?, ?>>();

   public AssetServer()
   {
   }

   public void register(ApplicationRuntime<?, ?, ?> assetManager)
   {
      runtimes.add(assetManager);
   }

   public void unregister(ApplicationRuntime<?, ?, ?> assetManager)
   {
      runtimes.remove(assetManager);
   }

   void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String path = req.getPathInfo();
      if (path != null && path.length() > 0)
      {
         for (ApplicationRuntime<?, ?, ?> runtime : runtimes)
         {
            if (runtime.getScriptManager().isClassPath(path) || runtime.getStylesheetManager().isClassPath(path))
            {
               InputStream in = runtime.getContext().getClassLoader().getResourceAsStream(path.substring(1));
               if (in != null)
               {
                  Tools.copy(in, resp.getOutputStream());
                  return;
               }
            }
         }
      }
   }
}
