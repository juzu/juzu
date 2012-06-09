package juzu.impl.template;

import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.template.metadata.TemplatesDescriptor;
import juzu.impl.utils.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatePlugin extends Plugin {

  public TemplatePlugin() {
    super("template");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return new TemplatesDescriptor(loader, config);
  }
}
