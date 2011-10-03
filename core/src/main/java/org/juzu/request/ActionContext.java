package org.juzu.request;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class ActionContext extends RequestContext
{

   public ActionContext(Map<String, String[]> parameters)
   {
      super(parameters);
   }
}
