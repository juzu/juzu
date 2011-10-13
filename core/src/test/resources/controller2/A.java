package controller2;

import org.juzu.Render;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Render
   public void noArg() { }

   @Render
   public void oneArg(String foo) { }

}
