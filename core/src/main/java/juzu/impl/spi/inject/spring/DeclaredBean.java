package juzu.impl.spi.inject.spring;

import juzu.Scope;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ScopeMetadata;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredBean extends AbstractBean
{

   /** . */
   private final Scope scope;
   
   DeclaredBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers)
   {
      super(type, qualifiers);
      
      //
      this.scope = scope;
   }

   @Override
   void configure(String name, SpringBuilder builder, DefaultListableBeanFactory factory)
   {
      AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(type);

      //
      if (scope != null)
      {
         definition.setScope(scope.name().toLowerCase());
      }
      else
      {
         ScopeMetadata scopeMD = builder.scopeResolver.resolveScopeMetadata(definition);
         if (scopeMD != null)
         {
            definition.setScope(scopeMD.getScopeName());
         }
      }

      //
      if (qualifiers != null)
      {
         for (AutowireCandidateQualifier qualifier : qualifiers)
         {
            definition.addQualifier(qualifier);
         }
      }

      //
      factory.registerBeanDefinition(name, definition);
   }
}
