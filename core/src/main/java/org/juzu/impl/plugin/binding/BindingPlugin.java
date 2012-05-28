package org.juzu.impl.plugin.binding;

import org.juzu.impl.application.Scope;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.JSON;
import org.juzu.plugin.binding.Bindings;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingPlugin extends Plugin
{

   public BindingPlugin()
   {
      super("binding");
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>, Scope>singletonMap(Bindings.class, Scope.APPLICATION);
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new BindingMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
      List<? extends JSON> bindings = config.getList("bindings", JSON.class);
      for (JSON binding : bindings)
      {
         String value = binding.getString("value");
         String scope = binding.getString("scope");
         String implementation = binding.getString("implementation");
         Class<?> valueType = loader.loadClass(value);
         Class<?> implementationType = implementation != null ? loader.loadClass(implementation) : null;
         org.juzu.Scope beanScope = scope != null ? org.juzu.Scope.valueOf(scope.toUpperCase()) : null;
         BeanDescriptor bean = new BeanDescriptor(valueType, beanScope, null, implementationType);
         beans.add(bean);
      }
      return new BindingDescriptor(beans);
   }
}
