package juzu.impl.controller;

import juzu.impl.controller.descriptor.ControllersDescriptor;
import juzu.impl.controller.descriptor.MethodDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.common.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin {

  /** . */
  private ControllersDescriptor descriptor;

  public ControllerPlugin() {
    super("controller");
  }

  public ControllersDescriptor getDescriptor() {
    return descriptor;
  }

  public ControllerResolver<MethodDescriptor> getResolver() {
    return descriptor != null ? descriptor.getResolver() : null;
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return descriptor = new ControllersDescriptor(loader, config);
  }
}
