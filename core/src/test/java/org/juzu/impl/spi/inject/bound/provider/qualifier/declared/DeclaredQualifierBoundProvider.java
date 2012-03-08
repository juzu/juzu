package org.juzu.impl.spi.inject.bound.provider.qualifier.declared;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierBoundProvider
{

   /** .*/
   private final String id = "" + Math.random();

   public String getId()
   {
      return id;
   }

   public static class Green extends DeclaredQualifierBoundProvider
   {
   }
}
