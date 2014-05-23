package examples.helloworld;

import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;

public class HelloWorldController {

  @Inject
  @Path("helloworld.gtmpl")
  Template helloworld;

  @View
  public Response index() {
    return helloworld.ok();
  }
}
