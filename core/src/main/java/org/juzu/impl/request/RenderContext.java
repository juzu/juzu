package org.juzu.impl.request;

import org.juzu.application.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class RenderContext extends MimeContext<RenderBridge>
{

   public RenderContext(ClassLoader classLoader, RenderBridge bridge)
   {
      super(classLoader, bridge);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.RENDER;
   }
}
