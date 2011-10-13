package org.juzu.impl.compiler;

import javax.lang.model.element.Element;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends Exception
{

   /** . */
   private final Element element;

   public CompilationException(Element element, String message)
   {
      super(message);

      //
      this.element = element;
   }

   public CompilationException(Element element, String message, Throwable cause)
   {
      super(message, cause);

      //
      this.element = element;
   }

   public Element getElement()
   {
      return element;
   }
}
