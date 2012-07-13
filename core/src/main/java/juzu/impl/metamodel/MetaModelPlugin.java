package juzu.impl.metamodel;

import juzu.impl.compiler.AnnotationData;
import juzu.impl.common.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelPlugin implements Serializable {

  /** The plugin name. */
  private final String name;

  protected MetaModelPlugin(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  /**
   * Returns the plugin descriptor or null.
   *
   * @param metaModel the meta model instance
   * @return the descriptor
   */
  public JSON getDescriptor(MetaModel metaModel) {
    return null;
  }

  /**
   * Returns a JSON representation mainly for testing purposes.
   *
   * @param metaModel the meta model instance
   * @return the json representation
   */
  public JSON toJSON(MetaModel metaModel) {
    return null;
  }

  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.emptySet();
  }

  public void init(MetaModel metaModel) {
  }

  public void postActivate(MetaModel metaModel) {
  }

  public void processAnnotation(MetaModel metaModel, Element element, String fqn, AnnotationData data) {
  }

  public void postProcessAnnotations(MetaModel metaModel) {
  }

  public void processEvents(MetaModel metaModel, EventQueue queue) {
  }

  public void postProcessEvents(MetaModel metaModel) {
  }

  public void prePassivate(MetaModel metaModel) {
  }
}
