package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext<B extends RequestBridge>
{

   /** The request classloader. */
   protected final ClassLoader classLoader;

   /** The request bridge. */
   protected final B bridge;

   public RequestContext(ClassLoader classLoader, B bridge)
   {
      this.classLoader = classLoader;
      this.bridge = bridge;
   }

   public final ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public final Map<String, String[]> getParameters()
   {
      return bridge.getParameters();
   }

   public abstract Phase getPhase();

   public final Object getContextualValue(Scope scope, Object key)
   {
      switch (scope)
      {
         case FLASH:
            return bridge.getFlashValue(key);
         case REQUEST:
         case MIME:
         case RENDER:
         case ACTION:
         case RESOURCE:
            return bridge.getRequestValue(key);
         case SESSION:
            return bridge.getSessionValue(key);
         case IDENTITY:
            return bridge.getIdentityValue(key);
         default:
            throw new AssertionError();
      }
   }

   public final void setContextualValue(Scope scope, Object key, Object value)
   {
      switch (scope)
      {
         case FLASH:
            bridge.setFlashValue(key, value);
            break;
         case ACTION:
         case RESOURCE:
         case MIME:
         case RENDER:
         case REQUEST:
            bridge.setRequestValue(key, value);
            break;
         case SESSION:
            bridge.setSessionValue(key, value);
            break;
         case IDENTITY:
            bridge.setIdentityValue(key, value);
            break;
         default:
            throw new AssertionError();
      }
   }
}
