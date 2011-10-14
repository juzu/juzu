package request.action;

import org.juzu.Action;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Action
   public void noArg() { }

   @Action
   public void oneArg(String foo) { }

}
