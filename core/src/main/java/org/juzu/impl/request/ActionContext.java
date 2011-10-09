package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class ActionContext extends RequestContext
{

   public ActionContext(Map<String, String[]> parameters)
   {
      super(parameters);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }
}
