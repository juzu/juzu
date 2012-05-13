package org.juzu.impl.asset;

import org.juzu.impl.application.ApplicationRuntime;
import org.juzu.impl.request.Request;
import org.juzu.impl.utils.NameLiteral;
import org.juzu.impl.utils.Path;
import org.juzu.impl.utils.Tools;
import org.juzu.request.HttpContext;
import org.juzu.request.RequestContext;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Server
{

   /** . */
   public static final Named PLUGIN = new NameLiteral("plugin");

   /** . */
   public static final Named APPLICATION = new NameLiteral("application");

   /** . */
   HashSet<ApplicationRuntime<?, ?, ?>> runtimes = new HashSet<ApplicationRuntime<?, ?, ?>>();

   /** . */
   final Multiplexer<String> mux = new Multiplexer<String>()
   {
      @Override
      public RouteContext getContext(final String key)
      {
         return new RouteContext()
         {
            @Override
            public void renderURL(Appendable out) throws IOException
            {
               RequestContext ctx = Request.getCurrent().getContext();
               HttpContext http = ctx.getHttpContext();
               out.append(http.getScheme()).append("://");
               out.append(http.getServerName());
               int port = http.getServerPort();
               if (port != 80)
               {
                  out.append(':').append(Integer.toString(port));
               }
               out.append(http.getContextPath());
               out.append("/assets/");
               out.append(key);
            }
         };
      }
   };

   /** . */
   private final Router applicationRouter;

   /** . */
   private final Router pluginRouter;

   public Server()
   {
      this.applicationRouter = mux.register(APPLICATION.value(), Router.class).getRoute();
      this.pluginRouter = mux.register(PLUGIN.value(), Router.class).getRoute();
   }

   public <R extends Route> Registration<R> register(final Scope scope, final String name, Class<R> routeType)
   {
      switch (scope)
      {
         case APPLICATION:
            return applicationRouter.register(name, routeType);
         case PLUGIN:
            return pluginRouter.register(name, routeType);
         default:
            throw new AssertionError();
      }
   }

   public Router getApplicationRouter()
   {
      return applicationRouter;
   }

   public Router getPluginRouter()
   {
      return pluginRouter;
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

         //
         Path p = Path.parse(path, '/');
         if (p.size() > 0)
         {
            mux.serve(p.get(0), p.next(), req, resp);
         }
      }
   }
}
