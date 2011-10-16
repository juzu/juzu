package org.juzu.utils;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Coordinate
{

   /** . */
   private final int offset;

   /** . */
   private final Location position;

   public Coordinate(int offset, Location position)
   {
      this.offset = offset;
      this.position = position;
   }

   public Coordinate(int offset, int col, int line)
   {
      this.offset = offset;
      this.position = new Location(col, line);
   }

   public int getOffset()
   {
      return offset;
   }

   public Location getPosition()
   {
      return position;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[offset=" + offset + ",position=" + position + "]";
   }
}
