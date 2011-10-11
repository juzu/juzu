package org.juzu.impl.spi.template.gtmpl;

import org.juzu.utils.Location;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Foo
{

   /** . */
   private final Location position;

   /** . */
   private final String value;

   public Foo(Location position, String value)
   {
      this.position = position;
      this.value = value;
   }

   public Location getPosition()
   {
      return position;
   }

   public String getValue()
   {
      return value;
   }
}
