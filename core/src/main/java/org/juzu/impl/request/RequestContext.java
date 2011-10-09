package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext
{

   /** . */
   protected final Map<String, String[]> parameters;

   public RequestContext(Map<String, String[]> parameters)
   {
      this.parameters = parameters;
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
