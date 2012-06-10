package juzu.impl.inject.spi.spring;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SingletonBean extends AbstractBean {

  /** . */
  final Object instance;

  SingletonBean(Object instance, Iterable<Annotation> qualifiers) {
    super(instance.getClass(), qualifiers);

    //
    this.instance = instance;
  }

  @Override
  void configure(String name, SpringBuilder builder, DefaultListableBeanFactory factory) {
    AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(type);
    definition.setScope("singleton");
    if (qualifiers != null) {
      for (AutowireCandidateQualifier qualifier : qualifiers) {
        definition.addQualifier(qualifier);
      }
    }
    factory.registerBeanDefinition(name, definition);

    // Register instance
    builder.instances.put(name, instance);
  }
}
