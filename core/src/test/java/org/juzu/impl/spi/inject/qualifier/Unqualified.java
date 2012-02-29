package org.juzu.impl.spi.inject.qualifier;

import org.juzu.impl.inject.Export;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Unqualified extends Qualifiable
{

   @Export
   public static class Red extends Qualified
   {
   }

   @Export
   public static class Green extends Qualified
   {
   }
}
