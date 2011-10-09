package org.juzu.impl.cdi;

import org.juzu.application.Phase;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
final class ContextImpl implements Context
{

   /** . */
   private final ScopeController controller;

   /** . */
   private final Class<? extends Annotation> scopeType;

   /** . */
   private final Phase phase;

   ContextImpl(ScopeController controller, Phase phase, Class<? extends Annotation> scopeType)
   {
      this.controller = controller;
      this.scopeType = scopeType;
      this.phase = phase;
   }

   public Class<? extends Annotation> getScope()
   {
      return scopeType;
   }

   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      return controller.get(phase, contextual, creationalContext);
   }

   public <T> T get(Contextual<T> contextual)
   {
      return get(contextual, null);
   }

   public boolean isActive()
   {
      return controller.currentPhase.get() == phase;
   }
}
