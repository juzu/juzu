package org.juzu.impl.spi.inject.cdi;

import javax.enterprise.context.Dependent;
import javax.inject.Scope;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractDeclaredBean extends AbstractBean
{

   /** . */
   private Class<? extends Annotation> scope;

   AbstractDeclaredBean(Class<?> type, Iterable<Annotation> qualifiers)
   {
      super(type, qualifiers);

      // Determine scope
      Class<? extends Annotation> scope = null;
      for (Annotation annotation : type.getAnnotations())
      {
         Class<? extends Annotation> annotationType = annotation.annotationType();
         if (annotationType.getAnnotation(Scope.class) != null)
         {
            scope = annotationType;
            break;
         }
      }
      if (scope == null)
      {
         scope = Dependent.class;
      }

      //
      this.scope = scope;
   }

   public Class<? extends Annotation> getScope()
   {
      return scope;
   }
}
