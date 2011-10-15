package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class ResourceContext extends MimeContext<ResourceBridge>
{

   public ResourceContext(ClassLoader classLoader, ResourceBridge bridge)
   {
      super(classLoader, bridge);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.RESOURCE;
   }

   @Override
   public Map<Object, Object> getContext(Scope scope)
   {
      switch (scope)
      {
         case FLASH:
            return bridge.getFlashContext();
         case RESOURCE:
         case REQUEST:
         case MIME:
            return bridge.getRequestContext();
         case RENDER:
         case ACTION:
            return null;
         case SESSION:
            return bridge.getSessionContext();
         case IDENTITY:
            return bridge.getIdentityContext();
         default:
            throw new AssertionError();
      }
   }
}
