package index;

import juzu.Response;
import juzu.View;

public class A {

  @View
  public Response.Render index() {
    return Response.ok("pass");
  }
}