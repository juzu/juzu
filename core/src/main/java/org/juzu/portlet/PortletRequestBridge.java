package org.juzu.portlet;

import org.juzu.impl.request.RequestBridge;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRequestBridge<Rq extends PortletRequest, Rs extends PortletResponse> implements RequestBridge
{

   /** . */
   protected final Rq request;
   
   /** . */
   protected final Rs response;

   public PortletRequestBridge(Rq request, Rs response)
   {
      this.request = request;
      this.response = response;
   }

   public Map<String, String[]> getParameters()
   {
      return request.getParameterMap();
   }

   public Map<Object, Object> getFlashContext()
   {
      return null;  
   }

   public Map<Object, Object> getRequestContext()
   {
      Map<Object, Object> store = (Map<Object, Object>)request.getAttribute("org.juzu.request_scope");
      if (store == null)
      {
         request.setAttribute("org.juzu.request_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public Map<Object, Object> getSessionContext()
   {
      PortletSession session = request.getPortletSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.session_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.session_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public Map<Object, Object> getIdentityContext()
   {
      return null;  
   }
}
