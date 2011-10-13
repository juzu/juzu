package org.juzu.portlet;

import org.juzu.Response;
import org.juzu.impl.request.ActionBridge;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionBridge extends PortletRequestBridge<ActionRequest, ActionResponse> implements ActionBridge
{

   /** . */
   private ResponseImpl response;

   public PortletActionBridge(ActionRequest actionRequest, ActionResponse actionResponse)
   {
      super(actionRequest, actionResponse);

      //
      this.response = null;
   }

   public Response createResponse()
   {
      if (response == null)
      {
         response = new ResponseImpl(super.response);
      }
      return response;
   }
}
