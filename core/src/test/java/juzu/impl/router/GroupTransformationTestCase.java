/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.router;

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.impl.router.regex.RERenderer;
import juzu.impl.router.regex.REVisitor;
import juzu.impl.router.regex.SyntaxException;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroupTransformationTestCase extends AbstractTestCase {

  @Test
  public void testCapturing() throws SyntaxException, IOException {
    assertCapturing("a", "(a)");
    assertCapturing("(a)", "(a)");
    assertCapturing("a(b)c", "a(b)c");
    assertCapturing("(a)?", "((?:a)?)");
    assertCapturing("a|b", "(a)|(b)");
    assertCapturing("(a)|b", "(a)|(b)");
    assertCapturing("(a|b)", "(a|b)");
    assertCapturing("(a)(b)", "((?:a)(?:b))");
    assertCapturing("(a)|", "(a)|()");
    assertCapturing("|(a)", "()|(a)");
    assertCapturing("|", "()|()");
  }

  @Test
  public void testNonCapturing() throws SyntaxException, IOException {
    assertNonCapturing("a", "(a)");
    assertNonCapturing("(a)", "((?:a))");
    assertNonCapturing("a(b)c", "(a(?:b)c)");
    assertNonCapturing("(a)|b", "((?:a)|b)");
  }

  private void assertNonCapturing(String test, String expected) throws SyntaxException, IOException {
    assertTransform(test, expected, false);
  }

  private void assertCapturing(String test, String expected) throws SyntaxException, IOException {
    assertTransform(test, expected, true);
  }

  private void assertTransform(String test, String expected, boolean capturing) throws SyntaxException, IOException {
    RENode node = new REParser(test).parse();
    REVisitor<RuntimeException> transformer = capturing ? new CaptureGroupTransformation() : new NonCaptureGroupTransformation();
    node.accept(transformer);
    StringBuilder sb = new StringBuilder();
    RERenderer renderer = new RERenderer(sb);
    node.accept(renderer);
    assertEquals(expected, sb.toString());
  }
}
