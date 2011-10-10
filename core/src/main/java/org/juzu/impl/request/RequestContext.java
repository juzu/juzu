package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext
{

   /** The request classloader. */
   protected final ClassLoader classLoader;

   /** . */
   protected final Map<String, String[]> parameters;

   public RequestContext(ClassLoader classLoader, Map<String, String[]> parameters)
   {
      this.classLoader = classLoader;
      this.parameters = parameters;
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
   public final Map<String, String[]> getParameters()
   {
      return parameters;
   }
}
