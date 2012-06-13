package juzu.impl.plugin.binding;

import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.utils.JSON;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingPlugin extends Plugin {

  public BindingPlugin() {
    super("binding");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
    List<? extends JSON> bindings = config.getList("bindings", JSON.class);
    for (JSON binding : bindings) {
      String value = binding.getString("value");
      String scope = binding.getString("scope");
      String implementation = binding.getString("implementation");
      Class<?> valueType = loader.loadClass(value);
      Class<?> implementationType = implementation != null ? loader.loadClass(implementation) : null;
      juzu.Scope beanScope = scope != null ? juzu.Scope.valueOf(scope.toUpperCase()) : null;
      BeanDescriptor bean = new BeanDescriptor(valueType, beanScope, null, implementationType);
      beans.add(bean);
    }
    return new BindingDescriptor(beans);
  }
}
