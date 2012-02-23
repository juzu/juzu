package org.juzu.impl.asset;

import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.impl.utils.Path;
import org.juzu.request.HttpContext;
import org.juzu.request.RequestContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Server
{

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
               RequestContext ctx = InternalApplicationContext.getCurrentRequest();
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
   private final PluginRouter pluginRouter;

   public Server()
   {
      this.applicationRouter = mux.register("application", Router.class).getRoute();
      this.pluginRouter = mux.register("plugin", PluginRouter.class).getRoute();
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

   public PluginRouter getPluginRouter()
   {
      return pluginRouter;
   }

   void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String uri = req.getPathInfo();
      if (uri != null && uri.length() > 0)
      {
         Path path = Path.parse(uri, '/');
         if (path.size() > 0)
         {
            mux.serve(path.get(0), path.next(), req, resp);
         }
      }
   }
}
