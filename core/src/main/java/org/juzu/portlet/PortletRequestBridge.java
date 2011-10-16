package org.juzu.portlet;

import org.juzu.impl.request.RequestBridge;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletRequestBridge<Rq extends PortletRequest, Rs extends PortletResponse> implements RequestBridge
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

   public final Map<String, String[]> getParameters()
   {
      return request.getParameterMap();
   }

   public void setFlashValue(Object key, Object value)
   {
      Map<Object, Object> flash = (Map<Object, Object>)getSessionValue("flash");
      if (flash == null)
      {
         setSessionValue("flash", flash = new HashMap<Object, Object>());
      }
      flash.put(key, value);
   }

   public Object getFlashValue(Object key)
   {
      Map<Object, Object> flash = (Map<Object, Object>)getSessionValue("flash");
      return flash != null ? flash.get(key) : null;
   }

   public Object getRequestValue(Object key)
   {
      return getRequestContext().get(key);
   }

   public void setRequestValue(Object key, Object value)
   {
      if (value != null)
      {
         getRequestContext().remove(key);
      }
      else
      {
         getRequestContext().put(key, value);
      }
   }

   private Map<Object, Object> getRequestContext()
   {
      Map<Object, Object> store = (Map<Object, Object>)request.getAttribute("org.juzu.request_scope");
      if (store == null)
      {
         request.setAttribute("org.juzu.request_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public void setSessionValue(Object key, Object value)
   {
      if (value != null)
      {
         getSessionContext().remove(key);
      }
      else
      {
         getSessionContext().put(key, value);
      }
   }

   public Object getSessionValue(Object key)
   {
      return getSessionContext().get(key);
   }

   private Map<Object, Object> getSessionContext()
   {
      PortletSession session = request.getPortletSession();
      Map<Object, Object> store = (Map<Object, Object>)session.getAttribute("org.juzu.session_scope");
      if (store == null)
      {
         session.setAttribute("org.juzu.session_scope", store = new HashMap<Object, Object>());
      }
      return store;
   }

   public Object getIdentityValue(Object key)
   {
      return null;
   }

   public void setIdentityValue(Object key, Object value)
   {
   }
}
