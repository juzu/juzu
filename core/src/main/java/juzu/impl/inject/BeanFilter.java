package juzu.impl.inject;

/**
 * A filter for beans.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface BeanFilter {

  /**
   * Determine if the bean should be accepted or rejected.
   *
   * @param beanType the bean type
   * @param <T>      the bean generic type
   * @return true if the bean is accepted
   */
  <T> boolean acceptBean(Class<T> beanType);

  BeanFilter DEFAULT = new BeanFilter() {
    public <T> boolean acceptBean(Class<T> beanType) {
      return !(beanType.getName().startsWith("juzu.") || beanType.getAnnotation(Export.class) != null);
    }
  };
}
