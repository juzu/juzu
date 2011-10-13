package org.juzu.portlet;

import org.juzu.Response;

import javax.portlet.ActionResponse;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ResponseImpl implements Response
{

   /** . */
   private final ActionResponse response;

   ResponseImpl(ActionResponse response)
   {
      this.response = response;
   }

   public void setParameter(String parameterName, String parameterValue)
   {
      response.setRenderParameter(parameterName, parameterValue);
   }
}
