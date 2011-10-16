package org.juzu.impl.template;

import org.juzu.utils.Coordinate;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetToken
{

   /** . */
   public int beginOffset;

   /** . */
   public int endOffset;

   public Coordinate getBegin()
   {
      return new Coordinate(beginOffset, ((Token)this).beginColumn, ((Token)this).beginLine);
   }

   public Coordinate getEnd()
   {
      return new Coordinate(endOffset, ((Token)this).endColumn, ((Token)this).endLine);
   }
}
