package org.juzu.impl.spi.inject.declared.provider.injected;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredProviderInjectedProduct
{

   /** . */
   public final DeclaredProviderInjectedDependency dependency;

   public DeclaredProviderInjectedProduct(DeclaredProviderInjectedDependency dependency)
   {
      this.dependency = dependency;
   }
}
