package juzu.impl.metadata;

import juzu.Scope;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Describes a bean registered in an IOC container.
 * <p/>
 * The {@link #declaredType} type is the mandatory type exposed by the bean. The {@link #implementationType} type can be
 * optionally provided to specify the implementation type of the bean. When the implementation type implements the
 * {@link javax.inject.Provider} interface, the implementation....
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class BeanDescriptor {

  /** The bean declared type. */
  private final Class<?> declaredType;

  /** The bean scope. */
  private final Scope scope;

  /** The bean qualifiers. */
  private final List<Annotation> qualifiers;

  /** The bean implementation type. */
  private final Class<?> implementationType;

  public BeanDescriptor(
    Class<?> declaredType,
    Scope scope,
    List<Annotation> qualifiers,
    Class<?> implementationType) throws NullPointerException, IllegalArgumentException {
    if (declaredType == null) {
      throw new NullPointerException("No null declared type accepted");
    }
    if (qualifiers != null) {
      for (Annotation qualifier : qualifiers) {
        if (qualifier.annotationType().getAnnotation(Qualifier.class) == null) {
          throw new IllegalArgumentException("Qualifier annotation " + qualifier + " is not annotated with @Qualifier");
        }
      }
    }

    //
    this.declaredType = declaredType;
    this.scope = scope;
    this.qualifiers = qualifiers;
    this.implementationType = implementationType;
  }

  public Class<?> getDeclaredType() {
    return declaredType;
  }

  public Scope getScope() {
    return scope;
  }

  public List<Annotation> getQualifiers() {
    return qualifiers;
  }

  public Class<?> getImplementationType() {
    return implementationType;
  }
}
