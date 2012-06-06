package juzu.plugin.less.impl.lesser;

import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Failure extends Result
{

   /** . */
   final LinkedList<LessError> errors = new LinkedList<LessError>();

   public LinkedList<LessError> getErrors()
   {
      return errors;
   }
}
