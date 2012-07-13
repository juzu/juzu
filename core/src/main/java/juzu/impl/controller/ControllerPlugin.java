package juzu.impl.controller;

import juzu.impl.controller.descriptor.ControllerDescriptor;
import juzu.impl.controller.descriptor.ControllerMethodResolver;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.common.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin {

  /** . */
  private ControllerMethodResolver resolver;

  public ControllerPlugin() {
    super("controller");
  }

  public ControllerMethodResolver getResolver() {
    return resolver;
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    ControllerDescriptor descriptor = new ControllerDescriptor(loader, config);
    resolver = new ControllerMethodResolver(descriptor);
    return descriptor;
  }
}
