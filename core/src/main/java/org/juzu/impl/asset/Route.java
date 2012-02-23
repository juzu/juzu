package org.juzu.impl.asset;

import org.juzu.impl.utils.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Route
{

   /** . */
   protected RouteContext context;
   
   void init(RouteContext context)
   {
      this.context = context;

      //
      init();
   }

   public RouteContext getContext()
   {
      return context;
   }

   public void init()
   {
   }

   public void destroy()
   {
   }

   /**
    * Returns true if the route served the request.
    *
    * @param path the path
    * @param req the request
    * @param resp the response
    * @return if the request was properly served
    * @throws java.io.IOException any io exception
    */
   public abstract boolean serve(Path path, HttpServletRequest req, HttpServletResponse resp) throws IOException;

}
