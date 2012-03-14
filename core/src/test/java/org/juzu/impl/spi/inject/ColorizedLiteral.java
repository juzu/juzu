package org.juzu.impl.spi.inject;

import javax.enterprise.util.AnnotationLiteral;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ColorizedLiteral extends AnnotationLiteral<Colorized> implements Colorized
{

   /** . */
   final Color value;

   public ColorizedLiteral(Color value)
   {
      this.value = value;
   }

   public Color value()
   {
      return value;
   }
}
