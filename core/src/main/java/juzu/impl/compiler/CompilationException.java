package juzu.impl.compiler;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends Exception {

  /** . */
  private List<CompilationError> errors;

  public CompilationException(List<CompilationError> errors) {
    this.errors = errors;
  }

  public List<CompilationError> getErrors() {
    return errors;
  }
}
