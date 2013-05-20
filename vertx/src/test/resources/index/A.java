package index;

import juzu.Response;
import juzu.View;

public class A {

  @View
  public Response.Content index() {
    return Response.ok("pass");
  }
}