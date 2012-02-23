package org.juzu.impl.asset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class Registration<R extends Route>
{

   /** . */
   private final Entry<?, R> entry;

   Registration(Entry<?, R> entry)
   {
      this.entry = entry;
   }
   
   public R getRoute()
   {
      return entry.route;
   }

   public void cancel()
   {
      entry.registrations.remove(this);
      if (entry.registrations.size() == 0)
      {
         entry.route.destroy();
         entry.multiplexer.map.remove(entry.key);
      }
   }
}
