package juzu.impl.plugin.portlet;

import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.Plugin;
import juzu.plugin.portlet.Portlet;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPlugin extends Plugin {

  public PortletPlugin() {
    super("portlet");
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.<Class<? extends Annotation>>singleton(Portlet.class);
  }

  @Override
  public ApplicationMetaModelPlugin newApplicationMetaModelPlugin() {
    return new PortletMetaModelPlugin();
  }
}
