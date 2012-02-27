package org.juzu.impl.spi.inject.lifecycle.unscoped;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LifeCycleUnscopedBean
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
