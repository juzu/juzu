package plugin.controller.error.invoke;

import juzu.Handler;
import juzu.Response;
import juzu.impl.plugin.controller.ControllerTestCase;
import juzu.request.Result;

public class ErrorHandler implements Handler<Result.Error, Response> {

  public ErrorHandler() {
  }

  @Override
  public Response handle(Result.Error argument) {
    ControllerTestCase.shared = argument;
    return Response.content(200, "hello");
  }
}