package org.juzu.impl.request;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface RequestBridge
{

   /**
    * Returns the request parameters.
    *
    * @return the request parameters
    */
   Map<String, String[]> getParameters();

   Map<Object, Object> getFlashContext();

   Map<Object, Object> getRequestContext();

   Map<Object, Object> getSessionContext();

   Map<Object, Object> getIdentityContext();

}
