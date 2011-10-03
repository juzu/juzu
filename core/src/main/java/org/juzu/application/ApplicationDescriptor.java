package org.juzu.application;

import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor
{

   /** . */
   private final String packageName;

   /** . */
   private final String name;

   /** . */
   private final List<ControllerMethod> controllerMethods;

   /** . */
   private final String templatesPackageName;

   public ApplicationDescriptor(
      String packageName,
      String name,
      String templatesPackageName,
      List<ControllerMethod> controllerMethods)
   {
      this.packageName = packageName;
      this.name = name;
      this.templatesPackageName = templatesPackageName;
      this.controllerMethods = Collections.unmodifiableList(controllerMethods);
   }

   public String getPackageName()
   {
      return packageName;
   }

   public String getName()
   {
      return name;
   }

   public List<ControllerMethod> getControllerMethods()
   {
      return controllerMethods;
   }

   public String getTemplatesPackageName()
   {
      return templatesPackageName;
   }
}
