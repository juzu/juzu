package juzu.impl.utils;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class NameLiteral extends AnnotationLiteral<Named> implements Named {

  /** . */
  private final String value;

  /**
   * Create a new name literal implementing the {@link Named} annotation interface.
   *
   * @param value the name value
   * @throws NullPointerException if the value is null
   */
  public NameLiteral(String value) throws NullPointerException {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }
}
