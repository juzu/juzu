package juzu.impl.application.metamodel;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.QN;

import javax.lang.model.element.Element;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BufKey implements Serializable {

  /** . */
  final QN pkg;

  /** . */
  final ElementHandle element;

  /** . */
  final String annotationFQN;

  BufKey(ProcessingContext env, Element element, String annotationFQN) {
    this.pkg = QN.parse(env.getPackageOf(element).getQualifiedName());
    this.element = ElementHandle.create(element);
    this.annotationFQN = annotationFQN;
  }

  @Override
  public int hashCode() {
    return element.hashCode() ^ annotationFQN.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof BufKey) {
      BufKey that = (BufKey)obj;
      return element.equals(that.element) && annotationFQN.equals(that.annotationFQN);
    }
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[element=" + element + ",annotation=" + annotationFQN + "]";
  }
}
