package juzu.impl.spi.inject.lifecycle.singleton;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class Bean
{

   /** . */
   public static int construct;

   /** . */
   public static int destroy;

   @PostConstruct
   public void create()
   {
      construct++;
   }

   @PreDestroy
   public void destroy()
   {
      destroy++;
   }
}
