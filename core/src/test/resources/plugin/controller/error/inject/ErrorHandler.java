package plugin.controller.error.inject;

import juzu.Handler;
import juzu.Response;
import juzu.impl.plugin.controller.ControllerTestCase;
import juzu.request.Result;

public class ErrorHandler implements Handler<Result.Error, Response> {

  @javax.inject.Inject A a;

  @Override
  public Response handle(Result.Error argument) {
    ControllerTestCase.shared = a;
    return Response.content(200, "hello");
  }
}