package juzu.plugin.less.impl.lesser;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JSR223Context extends JSContext
{

   /** . */
   private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

   @Override
   public void put(String name, Object value)
   {
      engine.put(name, value);
   }

   @Override
   public Object eval(String script) throws Exception
   {
      return engine.eval(script);
   }

   @Override
   public Object invokeFunction(String name, Object... args) throws Exception
   {
      return ((Invocable)engine).invokeFunction(name, args);
   }
}
