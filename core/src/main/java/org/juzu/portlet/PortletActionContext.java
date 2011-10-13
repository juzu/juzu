package org.juzu.portlet;

import org.juzu.Response;
import org.juzu.impl.request.ActionContext;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletActionContext extends ActionContext
{

   /** . */
   private final ActionRequest actionRequest;

   /** . */
   private final ActionResponse actionResponse;

   /** . */
   private ResponseImpl response;

   public PortletActionContext(ClassLoader classLoader, ActionRequest actionRequest, ActionResponse actionResponse)
   {
      super(classLoader);

      //
      this.actionRequest = actionRequest;
      this.actionResponse = actionResponse;
      this.response = null;
   }

   @Override
   public Map<String, String[]> getParameters()
   {
      return actionRequest.getParameterMap();
   }

   @Override
   public Response createResponse()
   {
      if (response == null)
      {
         response = new ResponseImpl(actionResponse);
      }
      return response;
   }
}
