package juzu.impl.application.metamodel;

import juzu.impl.compiler.AnnotationData;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.utils.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;

/**
 * A plugin for meta model processing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationMetaModelPlugin implements Serializable {

  public void init(ApplicationsMetaModel applications) {
  }

  public void postActivateApplicationsMetaModel(ApplicationsMetaModel applications) {
  }

  public void postActivate(ApplicationMetaModel application) {
  }

  public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data) {
  }

  public void postProcessAnnotations(ApplicationMetaModel application) {
  }

  public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event) {
  }

  public void postProcessEvents(ApplicationMetaModel application) {
  }

  public void prePassivate(ApplicationMetaModel model) {
  }

  public void prePassivate(ApplicationsMetaModel applications) {
  }

  public void postConstruct(ApplicationMetaModel application) {
  }

  public void preDestroy(ApplicationMetaModel application) {
  }

  /**
   * Returns the plugin descriptor for the specified application or null if the plugin should not be involved at
   * runtime.
   *
   * @param application the application
   * @return the descriptor
   */
  public JSON getDescriptor(ApplicationMetaModel application) {
    return null;
  }
}
