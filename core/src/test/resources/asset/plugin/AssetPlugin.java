package asset.plugin;

import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.asset.Registration;
import org.juzu.impl.asset.Route;
import org.juzu.impl.asset.Router;
import org.juzu.impl.request.LifeCyclePlugin;
import org.juzu.impl.request.Request;
import org.juzu.impl.utils.Path;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class AssetPlugin extends LifeCyclePlugin
{

   public static class RouteImpl extends Route
   {

      @Override
      public boolean serve(Path path, HttpServletRequest req, HttpServletResponse resp) throws IOException
      {
         resp.setContentType("text/html");
         resp.getWriter().print("<html><body>foo</body></html>");
         return true;
      }
   }
   
   /** . */
   private final Router router;

   /** . */
   private final Registration<RouteImpl> registration;

   @Inject
   public AssetPlugin(@Named("plugin") Router router)
   {
      this.registration = router.register("myplugin", RouteImpl.class);
      this.router = router;
   }

   @PreDestroy
   public void destroy()
   {
      registration.cancel();
   }

   @Override
   public void invoke(Request request) throws ApplicationException
   {
      super.invoke(request);

      //
      try
      {
         StringBuilder sb = new StringBuilder();
         registration.getRoute().getContext().renderURL(sb);
         request.setResponse(Response.render(sb.toString()));
      }
      catch (IOException e)
      {
         throw new ApplicationException(e);
      }
   }
}
