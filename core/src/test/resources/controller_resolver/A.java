package controller_resolver;

import org.juzu.Binding;
import org.juzu.Render;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Render
   public void noArg() { }

   @Render
   public void fooArg(String foo) { }

   @Render(parameters = @Binding(name ="foo", value = "foo_value"))
   public void fooBinding() { }

   @Render(parameters = @Binding(name ="bar", value = "bar_value"))
   public void barBindingFooArg(String foo) { }

   @Render(parameters = @Binding(name ="foo", value = "foo_value"))
   public void fooBindingBarArg(String bar) { }


/*

   @Render(parameters = @Binding(name ="foo", value = "foo_value"))
   public void bindingOneArg(String bar) { }
*/
}
