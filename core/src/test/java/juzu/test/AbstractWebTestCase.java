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
import juzu.impl.common.QN;
import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.test.protocol.portlet.AbstractPortletTestCase;
import juzu.test.protocol.standalone.AbstractStandaloneTestCase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractWebTestCase extends AbstractTestCase {

  /** . */
  private static QN applicationName;

  /** . */
  private static boolean asDefault;

  /**
   * Returns the currently deployed application name.
   *
   * @return the application name
   */
  public static QN getApplicationName() {
    return applicationName;
  }

  /**
   * Returns true if the currently application name should be the defaulted.
   *
   * @return the as default value
   */
  public static boolean asDefault() {
    return asDefault;
  }

  public static WebArchive createServletDeployment(String applicationName) {
    return createServletDeployment(false, applicationName);
  }

  public static WebArchive createServletDeployment(boolean asDefault, String... applicationNames) {

    //
    QN[] applicationQNs = new QN[applicationNames.length];
    QN packageQN = null;
    for (int i = 0;i < applicationNames.length;i++) {
      QN applicationQN = QN.parse(applicationNames[i]);
      applicationQNs[i] = applicationQN;
      packageQN = packageQN == null ? applicationQN : packageQN.getPrefix(applicationQN);
    }

    // Create war
    WebArchive war = createDeployment(packageQN);

    // Descriptor
    URL descriptor = AbstractStandaloneTestCase.class.getResource("web.xml");
    war.setWebXML(descriptor);

    // Set application name (maybe remove that)
    AbstractWebTestCase.applicationName = applicationQNs.length > 0 ? applicationQNs[0] : null;
    AbstractWebTestCase.asDefault = asDefault;

    //
    return war;
  }

  public static WebArchive createPortletDeployment(String packageName) {

    //
    QN packageQN = QN.parse(packageName);

    // Create war
    WebArchive war = createDeployment(packageQN);

    // Descriptor
    war.setWebXML(AbstractPortletTestCase.class.getResource("web.xml"));
    war.addAsWebInfResource(AbstractPortletTestCase.class.getResource("portlet.xml"), "portlet.xml");

    // Add libraries we need
/*
    war.addAsLibraries(DependencyResolvers.
        use(MavenDependencyResolver.class).
        loadEffectivePom("pom.xml")
        .artifacts("javax.servlet:jstl", "taglibs:standard").
            resolveAsFiles());
*/

    // Set application name (maybe remove that)
    applicationName = packageQN;

    //
    return war;
  }

  private static WebArchive createDeployment(QN pkgName) {

    // Compile classes
    CompilerAssert<File, File> compiler = compiler(false, pkgName, null);
    compiler.assertCompile();

    ReadWriteFileSystem<File> classOutput = compiler.getClassOutput();

    // Create war
    final WebArchive war = ShrinkWrap.create(WebArchive.class, "juzu.war");

    // Add output to war
    try {
      classOutput.traverse(new Visitor.Default<File>() {

        LinkedList<String> path = new LinkedList<String>();

        @Override
        public void enterDir(File dir, String name) throws IOException {
          path.addLast(name.isEmpty() ? "classes" : name);
        }

        @Override
        public void leaveDir(File dir, String name) throws IOException {
          path.removeLast();
        }

        @Override
        public void file(File file, String name) throws IOException {
          path.addLast(name);
          String target = Tools.join('/', path);
          path.removeLast();
          war.addAsWebInfResource(new ByteArrayAsset(new FileInputStream(file)), target);
        }
      });
    }
    catch (IOException e) {
      throw failure(e);
    }

    //
    return war;
  }

  @ArquillianResource
  protected URL deploymentURL;

  /**
   * Returns the portlet URL for standalone portlet unit test.
   *
   * @return the base portlet URL
   */
  public URL getPortletURL() {
    try {
      return deploymentURL.toURI().resolve("embed/StandalonePortlet").toURL();
    }
    catch (Exception e) {
      throw failure(e);
    }
  }

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
    return new UserAgent(applicationURL());
  }

  public URL applicationURL() {
    return applicationURL("");
  }

  public URL applicationURL(String path) {
    try {
      return deploymentURL.toURI().resolve(getApplicationName().getLastName() + path).toURL();
    }
    catch (Exception e) {
      throw failure("Could not build application url " + path, e);
    }
  }
}
