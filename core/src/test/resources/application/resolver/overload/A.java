package application.resolver.overload;

import org.juzu.View;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @View
   public void m()
   {
   }

   @View
   public void m(String foo)
   {
   }

   @View
   public void m(String foo, String bar)
   {
   }
}
