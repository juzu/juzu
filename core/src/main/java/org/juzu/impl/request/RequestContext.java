package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext<B extends RequestBridge>
{

   /** The request classloader. */
   protected final ClassLoader classLoader;

   /** The request bridge. */
   protected final B bridge;

   public RequestContext(ClassLoader classLoader, B bridge)
   {
      this.classLoader = classLoader;
      this.bridge = bridge;
   }

   public final ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public final Map<String, String[]> getParameters()
   {
      return bridge.getParameters();
   }

   public abstract Phase getPhase();

   public abstract Map<Object, Object> getContext(Scope scope);

}
