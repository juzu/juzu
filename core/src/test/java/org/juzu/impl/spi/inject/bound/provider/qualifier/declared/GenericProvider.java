package org.juzu.impl.spi.inject.bound.provider.qualifier.declared;

import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GenericProvider implements Provider<DeclaredQualifierBoundProvider>
{

   /** . */
   private final DeclaredQualifierBoundProvider product;

   public GenericProvider(DeclaredQualifierBoundProvider product)
   {
      this.product = product;
   }

   public DeclaredQualifierBoundProvider get()
   {
      return product;
   }
}
