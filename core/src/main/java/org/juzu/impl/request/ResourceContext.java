package org.juzu.impl.request;

import org.juzu.application.Phase;

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
}
