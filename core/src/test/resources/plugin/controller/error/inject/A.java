package plugin.controller.error.inject;

import java.util.ConcurrentModificationException;

public class A {

  @juzu.View
  public void index() throws Exception {
    throw new ConcurrentModificationException();
  }

}