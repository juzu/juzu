package plugin.controller.error.invoke;

import java.util.ConcurrentModificationException;

public class A {

  @juzu.View
  public void index() throws Exception {
    throw new ConcurrentModificationException();
  }

}