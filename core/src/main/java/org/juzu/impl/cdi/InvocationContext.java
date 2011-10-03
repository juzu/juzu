package org.juzu.impl.cdi;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class InvocationContext implements Context
{

   /** . */
   private final ThreadLocal<Map<Contextual<?>, Object>> current = new ThreadLocal<Map<Contextual<?>, Object>>();

   /** . */
   private static final Map<Contextual<?>, Object> EMPTY_MAP = Collections.emptyMap();

   /** . */
   private static final InvocationContext INSTANCE = new InvocationContext();

   public static InvocationContext getInstance()
   {
      return INSTANCE;
   }

   public static void start() throws IllegalStateException
   {
      if (INSTANCE.current.get() != null)
      {
         throw new IllegalStateException("Already started");
      }
      INSTANCE.current.set(EMPTY_MAP);
   }

   public static void stop()
   {
      INSTANCE.current.set(null);
   }

   public Class<? extends Annotation> getScope()
   {
      return InvocationScoped.class;
   }

   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
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

   public <T> T get(Contextual<T> contextual)
   {
      return get(contextual, null);
   }

   public boolean isActive()
   {
      return current.get() != null;
   }
}
