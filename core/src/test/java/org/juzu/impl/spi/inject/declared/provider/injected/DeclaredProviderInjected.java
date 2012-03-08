package org.juzu.impl.spi.inject.declared.provider.injected;

import javax.inject.Inject;
import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredProviderInjected implements Provider<DeclaredProviderInjectedProduct>
{

   @Inject
   private DeclaredProviderInjectedDependency dependency;

   public DeclaredProviderInjected()
   {
      System.out.println("FOO");
   }

   public DeclaredProviderInjectedProduct get()
   {
      return new DeclaredProviderInjectedProduct(dependency);
   }
}
