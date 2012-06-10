package juzu.impl.template.spi;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateException extends Exception {

  public TemplateException() {
  }

  public TemplateException(String message) {
    super(message);
  }

  public TemplateException(String message, Throwable cause) {
    super(message, cause);
  }

  public TemplateException(Throwable cause) {
    super(cause);
  }
}
