package juzu.impl.utils;

/**
 * A simple mime type enumeration that is used when a URL is generated.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum MimeType
{

  XHTML("&amp;"), PLAIN("&");

  public final String amp;

  private MimeType(String amp) {
    this.amp = amp;
  }
}
