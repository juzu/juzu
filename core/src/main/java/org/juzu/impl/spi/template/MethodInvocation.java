package org.juzu.impl.spi.template;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodInvocation
{

   /** . */
   private final String className;

   /** . */
   private final String methodName;

   /** . */
   private final List<String> methodArguments;

   public MethodInvocation(String className, String methodName, List<String> methodArguments)
   {
      this.className = className;
      this.methodName = methodName;
      this.methodArguments = methodArguments;
   }

   public String getClassName()
   {
      return className;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public List<String> getMethodArguments()
   {
      return methodArguments;
   }
}
