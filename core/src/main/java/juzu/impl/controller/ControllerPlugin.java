package juzu.impl.controller;

import juzu.impl.controller.descriptor.ControllerDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.utils.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin {

  public ControllerPlugin() {
    super("controller");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return new ControllerDescriptor(loader, config);
  }
}
