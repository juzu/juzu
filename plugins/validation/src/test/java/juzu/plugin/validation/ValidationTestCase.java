/*
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
package juzu.plugin.validation;

import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import juzu.test.protocol.mock.MockViewBridge;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ValidationTestCase extends AbstractInjectTestCase {

  public static Object o;

  public ValidationTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testValidation() throws Exception {
    MockApplication app = application("juzu.simple").init();
    MockClient client = app.client();
    MockViewBridge render = client.render();
    ValidationError va = assertInstanceOf(ValidationError.class, render.getResponse());
    Set<ConstraintViolation<Object>> violations = va.getViolations();
    assertEquals(1, violations.size());
    ConstraintViolation<Object> violation = violations.iterator().next();
    assertEquals("may not be null", violation.getMessage());
    Iterator<Path.Node> path = violation.getPropertyPath().iterator();
    Path.Node n1 = path.next();
    assertEquals("index", n1.getName());
    assertEquals(ElementKind.METHOD, n1.getKind());
    Path.Node n2 = path.next();
    assertEquals("mandatory", n2.getName());
    assertEquals(ElementKind.PARAMETER, n2.getKind());
    assertFalse(path.hasNext());
  }

  @Test
  public void testLifeCycle() throws Exception {
    MockApplication app = application("juzu.lifecycle").init();
    MockClient client = app.client();
    o = null;
    client.render();
    ValidationError error = assertInstanceOf(ValidationError.class, o);
    Set<ConstraintViolation<Object>> violations = error.getViolations();
    assertEquals(1, violations.size());
  }
}
