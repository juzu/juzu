package juzu.impl.spi.inject.spring;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractBean
{

   /** . */
   final Class<?> type;
   
   /** . */
   final List<AutowireCandidateQualifier> qualifiers;

   AbstractBean(Class<?> type, Iterable<Annotation> qualifiers)
   {
      List<AutowireCandidateQualifier> list = null;
      if (qualifiers != null)
      {
         list = new ArrayList<AutowireCandidateQualifier>();
         for (Annotation annotation : qualifiers)
         {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            AutowireCandidateQualifier md = new AutowireCandidateQualifier(annotationType.getName());
            for (Method method : annotationType.getMethods())
            {
               if (method.getParameterTypes().length == 0 && method.getDeclaringClass() != Object.class)
               {
                  try
                  {
                     String attrName = method.getName();
                     Object attrValue = method.invoke(annotation);
                     md.addMetadataAttribute(new BeanMetadataAttribute(attrName, attrValue));
                  }
                  catch (Exception e)
                  {
                     throw new UnsupportedOperationException("handle me gracefully", e);
                  }
               }
            }
            list.add(md);
         }
      }

      //
      this.type = type;
      this.qualifiers = list;
   }

   abstract void configure(String name, SpringBuilder builder, DefaultListableBeanFactory factory);

}
