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

   Object getFlashValue(Object key);

   void setFlashValue(Object key, Object value);

   Object getRequestValue(Object key);

   void setRequestValue(Object key, Object value);

   Object getSessionValue(Object key);

   void setSessionValue(Object key, Object value);

   Object getIdentityValue(Object key);

   void setIdentityValue(Object key, Object value);

}
