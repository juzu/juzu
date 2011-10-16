package request.scope.render;

import org.juzu.Action;
import org.juzu.Render;
import org.juzu.RenderScoped;
import org.juzu.Resource;
import org.juzu.URLBuilder;
import org.juzu.test.Registry;
import org.juzu.test.support.Car;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Produces
   @RenderScoped
   public static Car create()
   {
      return new Car();
   }

   @Action
   public void action()
   {
      try
      {
         long code = car.getIdentityHashCode();
         Registry.set("car", code);
      }
      catch (ContextNotActiveException expected)
      {
      }
   }

   @Inject
   private Car car;

   @Render
   public void index()
   {
      Registry.set("car", car.getIdentityHashCode());
      Registry.set("action", A_.actionURL().toString());
      Registry.set("resource", A_.resourceURL().toString());
   }

   @Resource
   public void resource()
   {
      try
      {
         long code = car.getIdentityHashCode();
         Registry.set("car", code);
      }
      catch (ContextNotActiveException expected)
      {
      }
   }
}
