package juzu.impl.common;

import juzu.test.AbstractTestCase;
import org.junit.Test;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodHandleTestCase extends AbstractTestCase {

  @Test
  public void testToString() {
    assertEquals("a#b()", new MethodHandle("a", "b").toString());
    assertEquals("a#b(c)", new MethodHandle("a", "b", "c").toString());
    assertEquals("a#b(c,d)", new MethodHandle("a", "b", "c", "d").toString());
  }

  @Test
  public void testInvalid() {
    String[] a = {
        "a",
        "a#",
        "a#b",
        "a#b(",
        "a#b(c",
        "a#b(,)",
    };
    for (String s : a) {
      try {
        MethodHandle.parse(s);
        fail();
      }
      catch (IllegalArgumentException ignore) {
      }
    }
  }



  @Test
  public void testParse() {
    assertEquals(new MethodHandle("a", "b"), MethodHandle.parse("a#b()"));
    assertEquals(new MethodHandle("a", "b", "c"), MethodHandle.parse("a#b(c)"));
    assertEquals(new MethodHandle("a", "b", "c", "d"), MethodHandle.parse("a#b(c,d)"));
  }
}
