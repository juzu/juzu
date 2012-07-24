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

package juzu.test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.protocol.mock.MockApplication;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractWebTestCase extends AbstractTestCase {

  @ArquillianResource
  protected URL deploymentURL;

  /** The currently deployed application. */
  private MockApplication<?> application;

  public final MockApplication<?> assertDeploy(String... packageName) {
    try {
      application = application(InjectImplementation.CDI_WELD, packageName);
      doDeploy(application);
      return application;
    }
    catch (Exception e) {
      throw failure("Could not deploy application " + Arrays.asList(packageName), e);
    }
  }

  @Override
  public void tearDown() {
    super.tearDown();
    if (application != null) {
      assertUndeploy();
    }
  }

  public void assertUndeploy() {
    if (application == null) {
      throw failure("No application to undeploy");
    }
    MockApplication<?> app = application;
    application = null;
    doUndeploy(app);
    app.getRuntime().shutdown();
  }
  protected abstract void doDeploy(MockApplication<?> application);

  protected abstract void doUndeploy(MockApplication<?> application);

  public void assertInternalError() {
    WebClient client = new WebClient();
    try {
      Page page = client.getPage(deploymentURL + "/juzu");
      throw failure("Was expecting an internal error instead of page " + page.toString());
    }
    catch (FailingHttpStatusCodeException e) {
      assertEquals(500, e.getStatusCode());
    }
    catch (IOException e) {
      throw failure("Was not expecting io exception", e);
    }
  }

  public UserAgent assertInitialPage() {
    return new UserAgent(deploymentURL);
  }
}
