package org.juzu.impl.spi.inject.lifecycle.scoped;

import org.juzu.SessionScoped;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SessionScoped
public class Bean
{

   /** . */
   public static int construct;

   /** . */
   public static int destroy;

   @PostConstruct
   public void construct()
   {
      construct++;
   }

   @PreDestroy
   public void destroy()
   {
      destroy++;
   }

   public void m()
   {
      // Here just to force a creation since we can have a proxy
   }
}
