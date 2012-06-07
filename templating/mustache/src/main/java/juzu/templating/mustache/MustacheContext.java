package juzu.templating.mustache;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MustacheContext implements Serializable {

  final String source;

  public MustacheContext(String source) {
    this.source = source;
  }
}
