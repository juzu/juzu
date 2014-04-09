package plugin.controller.error.inject;

import juzu.Handler;
import juzu.Response;
import juzu.impl.plugin.controller.ControllerTestCase;

public class ErrorHandler implements Handler<Response.Error, Response> {

  @javax.inject.Inject A a;

  @Override
  public Response handle(Response.Error argument) {
    ControllerTestCase.shared = a;
    return Response.content(200, "hello");
  }
}