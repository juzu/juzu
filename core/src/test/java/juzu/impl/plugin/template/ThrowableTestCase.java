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

package juzu.impl.plugin.template;

import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.template.TemplateExecutionException;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import juzu.test.protocol.mock.MockClient;
import org.junit.Test;

import javax.naming.AuthenticationException;
import java.util.ConcurrentModificationException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ThrowableTestCase extends AbstractInjectTestCase {

  public ThrowableTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testChecked() throws Exception {
    MockApplication<?> app = application("plugin", "template", "throwable", "checked").init();

    //
    MockClient client = app.client();
    try {
      client.render();
    }
    catch (ApplicationException e) {
      assertInstanceOf(AuthenticationException.class, assertInstanceOf(TemplateExecutionException.class, e.getCause()).getCause());
    }
  }

  @Test
  public void testRuntime() throws Exception {
    MockApplication<?> app = application("plugin", "template", "throwable", "runtime").init();

    //
    MockClient client = app.client();
    try {
      client.render();
    }
    catch (ApplicationException e) {
      assertInstanceOf(ConcurrentModificationException.class, assertInstanceOf(TemplateExecutionException.class, e.getCause()).getCause());
    }
  }

  @Test
  public void testError() throws Exception {
    MockApplication<?> app = application("plugin", "template", "throwable", "error").init();

    //
    MockClient client = app.client();
    try {
      client.render();
    }
    catch (ApplicationException e) {
      assertInstanceOf(UnknownError.class, e.getCause()).getCause();
    }
  }
}
