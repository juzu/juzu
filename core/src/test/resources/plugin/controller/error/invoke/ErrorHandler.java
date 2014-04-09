package plugin.controller.error.invoke;

import juzu.Handler;
import juzu.Response;
import juzu.impl.plugin.controller.ControllerTestCase;

public class ErrorHandler implements Handler<Response.Error, Response> {

  public ErrorHandler() {
  }

  @Override
  public Response handle(Response.Error argument) {
    ControllerTestCase.shared = argument;
    return Response.content(200, "hello");
  }
}