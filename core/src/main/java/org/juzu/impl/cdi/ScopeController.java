package org.juzu.impl.cdi;

import org.juzu.ActionScoped;
import org.juzu.RenderScoped;
import org.juzu.RequestScoped;
import org.juzu.application.Phase;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeController
{

   /** . */
   private static final Map<Contextual<?>, Object> EMPTY_MAP = Collections.emptyMap();

   /** . */
   static final ScopeController INSTANCE = new ScopeController();

   /** . */
   final ThreadLocal<Map<Contextual<?>, Object>> current = new ThreadLocal<Map<Contextual<?>, Object>>();

   /** . */
   final ThreadLocal<Phase> currentPhase = new ThreadLocal<Phase>();

   /** . */
   final ContextImpl requestContext = new ContextImpl(this, null, RequestScoped.class);

   /** . */
   final ContextImpl actionContext = new ContextImpl(this, Phase.ACTION, ActionScoped.class);

   /** . */
   final ContextImpl renderContext = new ContextImpl(this, Phase.RENDER, RenderScoped.class);

   public static void start(Phase phase) throws IllegalStateException
   {
      if (phase == null)
      {
         throw new NullPointerException();
      }
      if (INSTANCE.current.get() != null)
      {
         throw new IllegalStateException("Already started");
      }
      INSTANCE.current.set(EMPTY_MAP);
      INSTANCE.currentPhase.set(phase);
   }

   public static void stop()
   {
      INSTANCE.current.set(null);
      INSTANCE.currentPhase.set(null);
   }

   public <T> T get(Phase phase, Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      Map<Contextual<?>, Object> map = current.get();
      if (map == null)
      {
         throw new ContextNotActiveException();
      }
      Object o = map.get(contextual);
      if (o == null)
      {
         if (creationalContext != null)
         {
            o = contextual.create(creationalContext);
            if (map == EMPTY_MAP)
            {
               current.set(map = new HashMap<Contextual<?>, Object>());
            }
            map.put(contextual, o);
         }
      }
      return (T)o;
   }

   public boolean isActive(Phase phase)
   {
      return phase == null || currentPhase.get() == phase;
   }
}
