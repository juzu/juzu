package org.juzu.impl.asset;

import org.juzu.impl.utils.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Router extends Route
{
   
   private final Multiplexer<String> mux = new Multiplexer<String>()
   {
      @Override
      public RouteContext getContext(final String key)
      {
         return new RouteContext()
         {
            @Override
            public void renderURL(Appendable out) throws IOException
            {
               context.renderURL(out);
               out.append('/');
               out.append(key);
            }
         };
      }
   };

   public <R extends Route> Registration<R> register(String name, Class<R> routeType)
   {
      return mux.register(name, routeType);
   }

   @Override
   public boolean serve(Path path, HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      if (path.size() > 0)
      {
         String name = path.get(0);
         return mux.serve(name, path.next(), req, resp);
      }
      return false;
   }
}
