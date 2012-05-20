package org.juzu.plugin.less.impl.lesser;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class JSContext
{

   public abstract void put(String name, Object value);

   public abstract Object eval(String script) throws Exception;

   public abstract Object invokeFunction(String name, Object... args) throws Exception;

}
