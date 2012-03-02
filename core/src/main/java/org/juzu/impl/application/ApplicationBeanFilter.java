package org.juzu.impl.application;

import org.juzu.impl.inject.BeanFilter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ApplicationBeanFilter implements BeanFilter
{

   public <T> boolean acceptBean(Class<T> beanType)
   {
      return false;
   }
}
