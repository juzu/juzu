package org.juzu.impl.request;

import org.juzu.application.Phase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum Scope
{

   RENDER()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return context.getPhase() == Phase.RENDER;
      }
   },

   ACTION()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return context.getPhase() == Phase.ACTION;
      }
   },

   REQUEST()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return true;
      }
   },

   RESOURCE()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return context.getPhase() == Phase.RESOURCE;
      }
   },

   MIME()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return context.getPhase() == Phase.RENDER || context.getPhase() == Phase.RESOURCE;
      }
   },

   SESSION()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return true;
      }
   },

   /**
    * todo : study more in depth how flash scoped is propagated to other phase, specially the resource phase
    * todo : that should kind of have an ID.
    */
   FLASH()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return true;
      }
   },

   IDENTITY()
   {
      @Override
      public boolean isActive(RequestContext<?> context)
      {
         return false;
      }
   };
   
   public abstract boolean isActive(RequestContext<?> context);

}
