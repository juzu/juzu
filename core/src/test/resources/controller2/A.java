package controller2;

import org.juzu.Binding;
import org.juzu.Render;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Render
   public void noArg() { }

   @Render
   public void oneArg(String foo) { }

   @Render(parameters = @Binding(name ="foo", value = "foo_value"))
   public void binding() { }

   @Render(parameters = @Binding(name ="foo", value = "foo_value"))
   public void bindingOneArg(String bar) { }
}
