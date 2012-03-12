package org.juzu.impl.plugin.binding;

import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.JSON;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedAnnotationTypes("org.juzu.plugin.binding.Bindings")
public class BindingPlugin extends Plugin
{

   public BindingPlugin()
   {
      super("binding");
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
         String implementation = binding.getString("implementation");
         Class<?> valueType = loader.loadClass(value);
         Class<?> implementationType = implementation != null ? loader.loadClass(implementation) : null;
         BeanDescriptor bean = new BeanDescriptor(valueType, null, implementationType);
         beans.add(bean);
      }
      return new BindingDescriptor(beans);
   }
}
