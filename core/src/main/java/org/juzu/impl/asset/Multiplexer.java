package org.juzu.impl.asset;

import org.juzu.impl.utils.Path;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class Multiplexer<K>
{

   /** . */
   final Map<K, Entry<K, ?>> map = new HashMap<K, Entry<K, ?>>();
   
   public Route get(K key)
   {
      Entry<K, ?> entry = map.get(key);
      return entry != null ? entry.route : null;
   }
   
   public abstract RouteContext getContext(K key);

   public <R extends Route> Registration<R> register(K key, final Class<R> routeType)
   {
      return register(key, new Provider<R>()
      {
         public R get()
         {
            try
            {
               return routeType.newInstance();
            }
            catch (InstantiationException e)
            {
               throw new UnsupportedOperationException("handle me gracefully", e);
            }
            catch (IllegalAccessException e)
            {
               throw new UnsupportedOperationException("handle me gracefully", e);
            }
         }
      });
   }

   public <R extends Route> Registration<R> register(K key, Provider<R> routeProvider)
   {
      //
      Entry<K, R> entry = (Entry<K, R>)map.get(key);

      //
      if (entry == null)
      {
         R route = routeProvider.get();

         //
         map.put(key, entry = new Entry<K, R>(this, key, route));

         //
         RouteContext context = getContext(key);

         //
         route.init(context);
      }

      //
      Registration<R> registration = new Registration<R>(entry);
      entry.registrations.add(registration);
      return registration;
   }

   boolean serve(K key, Path next, HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      Route route = get(key);
      return route != null && route.serve(next, req, resp);
   }
}
