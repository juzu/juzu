package resolvebean;

import javax.inject.Named;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Named("foo")
public class B {

  @Override
  public String toString() {
    return "bar";
  }
}
