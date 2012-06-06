package org.sample;

import juzu.Path;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Controller {

  @Inject
  @Path("index.gtmpl")
  Template index;


  @View
  public void index() throws IOException {
    index.render();
  }
}
