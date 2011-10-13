package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext
{

   /** The request classloader. */
   protected final ClassLoader classLoader;

   public RequestContext(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public final ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public abstract Phase getPhase();

   /**
    * Returns the request parameters.
    *
    * @return the request parameters
    */
   public abstract Map<String, String[]> getParameters();
}
