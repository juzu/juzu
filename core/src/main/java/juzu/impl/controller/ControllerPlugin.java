package juzu.impl.controller;

import juzu.Action;
import juzu.Resource;
import juzu.View;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.controller.descriptor.ControllerDescriptor;
import juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Tools;

import java.lang.annotation.Annotation;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin {

  public ControllerPlugin() {
    super("controller");
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Tools.<Class<? extends Annotation>>set(View.class, Action.class, Resource.class);
  }

  @Override
  public ApplicationMetaModelPlugin newApplicationMetaModelPlugin() {
    return new ControllerMetaModelPlugin();
  }

  @Override
  public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception {
    return new ControllerDescriptor(loader, config);
  }
}
