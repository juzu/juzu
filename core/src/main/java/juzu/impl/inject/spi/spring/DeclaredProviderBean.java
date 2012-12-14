package juzu.impl.inject.spi.spring;

import juzu.Scope;
import juzu.impl.utils.Tools;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ScopeMetadata;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredProviderBean extends AbstractBean {

  /** . */
  private final Class<? extends Provider> providerType;

  /** . */
  private final Scope scope;

  DeclaredProviderBean(
    Class<?> type,
    Scope scope,
    Iterable<Annotation> qualifiers,
    Class<? extends Provider> providerType) {
    super(type, qualifiers);

    //
    this.scope = scope;
    this.providerType = providerType;
  }

  @Override
  void configure(String name, SpringBuilder builder, DefaultListableBeanFactory factory) {
    String _name = Tools.nextUUID();
    AnnotatedGenericBeanDefinition _definition = new AnnotatedGenericBeanDefinition(providerType);
    _definition.setScope("singleton");
    factory.registerBeanDefinition(_name, _definition);

    //
    AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(type);

    //
    if (scope != null) {
      definition.setScope(scope.name().toLowerCase());
    }
    else {
      ScopeMetadata scopeMD = builder.scopeResolver.resolveScopeMetadata(definition);
      if (scopeMD != null) {
        definition.setScope(scopeMD.getScopeName());
      }
    }

    //
    if (qualifiers != null) {
      for (AutowireCandidateQualifier qualifier : qualifiers) {
        definition.addQualifier(qualifier);
      }
    }

    //
    if (qualifiers != null) {
      for (AutowireCandidateQualifier qualifier : qualifiers) {
        definition.addQualifier(qualifier);
      }
    }

    //
    definition.setFactoryBeanName(_name);
    definition.setFactoryMethodName("get");

    //
    factory.registerBeanDefinition(name, definition);
  }
}
