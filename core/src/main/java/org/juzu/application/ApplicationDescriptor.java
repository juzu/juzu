package org.juzu.application;

import org.juzu.impl.request.ControllerMethod;

import java.lang.reflect.Method;
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
   private Class<?> defaultController;

   /** . */
   private final List<ControllerMethod> controllerMethods;

   /** . */
   private final String templatesPackageName;

   public ApplicationDescriptor(
      String packageName,
      String name,
      Class<?> defaultController,
      String templatesPackageName,
      List<ControllerMethod> controllerMethods)
   {
      this.defaultController = defaultController;
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

   public Class<?> getDefaultController()
   {
      return defaultController;
   }

   public List<ControllerMethod> getControllerMethods()
   {
      return controllerMethods;
   }

   public ControllerMethod getControllerMethod(Class<?> type, String name, Class<?>... parameterTypes)
   {
      for (int i = 0;i < controllerMethods.size();i++)
      {
         ControllerMethod cm = controllerMethods.get(i);
         Method m = cm.getMethod();
         if (type.equals(cm.getType()) && m.getName().equals(name))
         {
            Class<?>[] a = m.getParameterTypes();
            if (a.length == parameterTypes.length)
            {
               for (int j = 0;j < parameterTypes.length;j++)
               {
                  if (!a[j].equals(parameterTypes[j]))
                  {
                     continue;
                  }
               }
               return cm;
            }
         }
      }
      return null;
   }

   public String getTemplatesPackageName()
   {
      return templatesPackageName;
   }
}
