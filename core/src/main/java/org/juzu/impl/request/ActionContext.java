package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class ActionContext extends RequestContext
{

   public ActionContext(ClassLoader classLoader, Map<String, String[]> parameters)
   {
      super(classLoader, parameters);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }
}
