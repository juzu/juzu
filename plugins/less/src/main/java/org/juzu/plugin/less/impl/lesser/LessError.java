package org.juzu.plugin.less.impl.lesser;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessError extends Result
{

   /** . */
   public final int line;

   /** . */
   public final int column;

   /** . */
   public final int index;

   /** . */
   public final String message;

   /** . */
   public final String type;

   /** . */
   public final String extract;

   public LessError(int line, int column, int index, String message, String type, String extract)
   {
      this.line = line;
      this.column = column;
      this.index = index;
      this.message = message;
      this.type = type;
      this.extract = extract;
   }

   @Override
   public String toString()
   {
      return "Failure[line=" + line +",column=" + column + ",index=" + index + ",message=" + message + ",type=" +
         type + ",extract=" + extract + "]";
   }
}
