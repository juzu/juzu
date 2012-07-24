/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
