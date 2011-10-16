package org.juzu.test.request;

import org.juzu.impl.request.RequestBridge;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockRequestBridge implements RequestBridge
{

   /** . */
   private final MockClient client;

   /** . */
   private final Map<String, String[]> parameters;

   /** . */
   private final Map<Object, Object> attributes;

   public MockRequestBridge(MockClient client)
   {
      this.client = client;
      this.parameters = new HashMap<String, String[]>();
      this.attributes = new HashMap<Object, Object>();

   }

   public Map<Object, Object> getAttributes()
   {
      return attributes;
   }

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public Object getFlashValue(Object key)
   {
      return client.getFlashValue(key);
   }

   public void setFlashValue(Object key, Object value)
   {
      client.setFlashValue(key, value);
   }

   public Object getRequestValue(Object key)
   {
      return attributes.get(key);
   }

   public void setRequestValue(Object key, Object value)
   {
      if (value != null)
      {
         attributes.put(key, value);
      }
      else
      {
         attributes.remove(key);
      }
   }

   public Object getSessionValue(Object key)
   {
      return client.getSession().get(key);
   }

   public void setSessionValue(Object key, Object value)
   {
      if (value != null)
      {
         client.getSession().put(key, value);
      }
      else
      {
         client.getSession().remove(key);
      }
   }

   public Object getIdentityValue(Object key)
   {
      return null;
   }

   public void setIdentityValue(Object key, Object value)
   {
   }
}
