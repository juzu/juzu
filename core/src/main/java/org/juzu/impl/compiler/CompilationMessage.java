package org.juzu.impl.compiler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationMessage
{

   /** . */
   private final MessageCode code;

   /** . */
   private final Object[] arguments;

   public CompilationMessage(MessageCode code, Object[] arguments)
   {
      this.code = code;
      this.arguments = arguments;
   }

   public MessageCode getCode()
   {
      return code;
   }

   public Object[] getArguments()
   {
      return arguments;
   }
}
