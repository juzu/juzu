package juzu.impl.plugin.ajax;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.common.JSON;
import juzu.plugin.ajax.Ajax;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private final HashMap<ElementHandle.Package, AtomicBoolean> enabledMap = new HashMap<ElementHandle.Package, AtomicBoolean>();

  public AjaxMetaModelPlugin() {
    super("ajax");
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.<Class<? extends Annotation>>singleton(Ajax.class);
  }

  @Override
  public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data) {
    if (fqn.equals(Ajax.class.getName())) {
      ElementHandle.Package handle = application.getHandle();
      AtomicBoolean enabled = enabledMap.get(handle);
      enabled.set(true);
    }
  }

  @Override
  public void postConstruct(ApplicationMetaModel application) {
    enabledMap.put(application.getHandle(), new AtomicBoolean(false));
  }

  @Override
  public void preDestroy(ApplicationMetaModel application) {
    enabledMap.remove(application.getHandle());
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    ElementHandle.Package handle = application.getHandle();
    AtomicBoolean enabled = enabledMap.get(handle);
    return enabled != null && enabled.get() ? new JSON() : null;
  }
}
