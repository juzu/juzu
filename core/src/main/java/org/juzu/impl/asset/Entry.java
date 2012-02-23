package org.juzu.impl.asset;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Entry<K, R extends Route>
{

   /** . */
   final Multiplexer<?> multiplexer;
   
   /** . */
   final K key;

   /** . */
   final R route;
   
   /** . */
   final List<Registration> registrations;

   Entry(Multiplexer<?> multiplexer, K key, R route)
   {
      this.multiplexer = multiplexer;
      this.key = key;
      this.route = route;
      this.registrations = new ArrayList<Registration>();
   }
}
