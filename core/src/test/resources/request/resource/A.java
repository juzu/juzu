package request.resource;

import org.juzu.Resource;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Resource
   public void noArg() { }

   @Resource
   public void oneArg(String foo) { }

}
