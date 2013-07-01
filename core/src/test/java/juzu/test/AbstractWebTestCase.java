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

package juzu.test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import juzu.impl.common.Name;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.test.protocol.portlet.JuzuPortlet;
import juzu.test.protocol.servlet.JuzuServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractWebTestCase extends AbstractTestCase {

  /** . */
  private static Name applicationName;

  /** . */
  private static String path;

  /** . */
  private static boolean asDefault;

  /** . */
  private static CompilerAssert<File, File> compiler;

  /** . */
  private static boolean servlet;

  /**
   * Returns the currently deployed application name.
   *
   * @return the application name
   */
  public static Name getApplicationName() {
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

  /**
   * Returns the compiler for the currently deployed web application.
   *
   * @return the compiler
   */
  public static CompilerAssert<File, File> getCompiler() {
    return compiler;
  }

  private static WebArchive createDeployment(boolean asDefault, String... applicationNames) {

    //
    Name[] applicationQNs = new Name[applicationNames.length];
    Name packageQN = null;
    for (int i = 0;i < applicationNames.length;i++) {
      Name applicationQN = Name.parse(applicationNames[i]);
      applicationQNs[i] = applicationQN;
      packageQN = packageQN == null ? applicationQN : packageQN.getPrefix(applicationQN);
    }

    // Compile classes
    CompilerAssert<File, File> compiler = compiler(false, packageQN);
    compiler.assertCompile();

    //
    ReadWriteFileSystem<File> classOutput = compiler.getClassOutput();

    // Create war
    final WebArchive war = ShrinkWrap.create(WebArchive.class, "juzu.war");

    // Add output to war
    try {
      classOutput.traverse(new Visitor.Default<File>() {

        /** . */
        StringBuffer path = new StringBuffer();

        @Override
        public void enterDir(File dir, String name) throws IOException {
          if (name.isEmpty()) {
            path.append("classes/");
          } else {
            path.append(name).append('/');
          }
        }

        @Override
        public void leaveDir(File dir, String name) throws IOException {
          if (name.isEmpty()) {
            path.setLength(0);
          } else {
            path.setLength(path.length() - name.length() - 1);
          }
        }

        @Override
        public void file(File file, String name) throws IOException {
          path.append(name);
          String target = path.toString();
          path.setLength(path.length() - name.length());
          war.addAsWebInfResource(new ByteArrayAsset(new FileInputStream(file)), target);
        }
      });
    }
    catch (IOException e) {
      throw failure(e);
    }

    // Set static state that we may need later
    AbstractWebTestCase.applicationName = applicationQNs.length > 0 ? applicationQNs[0] : null;
    AbstractWebTestCase.asDefault = asDefault;
    AbstractWebTestCase.compiler = compiler;

    //
    return war;
  }

  public static WebArchive createServletDeployment(String applicationName) {
    return createServletDeployment(false, applicationName);
  }
  
  public static WebArchive createServletDeployment(InjectorProvider injector, String applicationName) {
    return createServletDeployment(new TestConfig().injector(injector), false, applicationName);
  }
  
  public static WebArchive createServletDeployment(boolean asDefault, String... applicationNames) {
    return createServletDeployment(RunMode.PROD, asDefault, applicationNames);
  }
  
  public static WebArchive createServletDeployment(String urlPattern, boolean asDefault, String... applicationNames) {
    return createServletDeployment(new TestConfig().urlPattern(urlPattern), asDefault, applicationNames);
  }
  
  public static WebArchive createServletDeployment(InjectorProvider injector, String urlPattern, boolean asDefault, String... applicationNames) {
    return createServletDeployment(new TestConfig().injector(injector).urlPattern(urlPattern), asDefault, applicationNames);
  }

  public static WebArchive createServletDeployment(
      RunMode runMode,
      boolean asDefault,
      String... applicationNames) {
    return createServletDeployment(new TestConfig().runMode(runMode), asDefault, applicationNames);
  }
  
  public static WebArchive createServletDeployment(
    InjectorProvider injector,
    RunMode runMode,
    boolean asDefault,
    String... applicationNames) {
  return createServletDeployment(new TestConfig().runMode(runMode).injector(injector), asDefault, applicationNames);
}
  
  public static WebArchive createServletDeployment(
      TestConfig config,
      boolean asDefault,
      String... applicationNames) {

    //
    String urlPattern = config.getURLPattern();
    RunMode runMode = config.getRunMode();
    InjectorProvider injector = config.getInjector();

    // Create war
    String path;
    if ("/".equals(urlPattern)) {
      path = "";
    } else if ("/*".equals(urlPattern)) {
      throw failure("Not yet implemented");
    } else if (urlPattern.startsWith("/") && urlPattern.endsWith("/*")) {
      path = urlPattern.substring(1, urlPattern.length() - 1);
    } else {
      throw failure("Illegal url pattern " + urlPattern);
    }

    //
    WebArchive war = createDeployment(asDefault, applicationNames);
    AbstractWebTestCase.path = path;
    AbstractWebTestCase.servlet = true;

    //
    String sourcePath;
    try {
      sourcePath = runMode.isDynamic() ? getCompiler().getSourcePath().getRoot().getCanonicalFile().getAbsolutePath() : "";
    }
    catch (IOException e) {
      throw failure("Could not read obtain source path", e);
    }

    // Descriptor
    String servlet;
    try {
      servlet = Tools.read(JuzuServlet.class.getResourceAsStream("web.xml"));
    }
    catch (IOException e) {
      throw failure("Could not read web xml deployment descriptor", e);
    }
    servlet = String.format(
        servlet,
        runMode.getValue(),
        sourcePath,
        injector.getValue(),
        config.getRequestEncoding().name(),
        urlPattern);

    // Descriptor
    war.setWebXML(new StringAsset(servlet));

    //
    return war;
  }

  public static WebArchive createPortletDeployment(String packageName) {
    return createPortletDeployment(InjectorProvider.INJECT_GUICE, RunMode.PROD, packageName);
  }
  
  public static WebArchive createPortletDeployment(InjectorProvider injector, String packageName) {
    return createPortletDeployment(injector, RunMode.PROD, packageName);
  }
  
  public static WebArchive createPortletDeployment(RunMode runMode, String packageName) {
    return createPortletDeployment(InjectorProvider.INJECT_GUICE, runMode, packageName);
  } 

  public static WebArchive createPortletDeployment(InjectorProvider injector, RunMode runMode, String packageName) {

    //
    WebArchive war = createDeployment(asDefault, packageName);
    AbstractWebTestCase.path = null;
    AbstractWebTestCase.servlet = false;

    //
    String sourcePath;
    try {
      sourcePath = runMode.isDynamic() ? getCompiler().getSourcePath().getRoot().getCanonicalFile().getAbsolutePath() : "";
    }
    catch (IOException e) {
      throw failure("Could not read obtain source path", e);
    }

    // portlet.xml descriptor
    String portlet;
    try {
      portlet = Tools.read(JuzuPortlet.class.getResourceAsStream("portlet.xml"));
    }
    catch (IOException e) {
      throw failure("Could not read portlet xml deployment descriptor", e);
    }
    portlet = String.format(
        portlet,
        injector.getValue());

    // web.xml descriptor
    String web;
    try {
      web = Tools.read(JuzuPortlet.class.getResourceAsStream("web.xml"));
    }
    catch (IOException e) {
      throw failure("Could not read portlet xml deployment descriptor", e);
    }
    web = String.format(
        web,
        runMode.getValue(),
        sourcePath);

    //
    war.setWebXML(new StringAsset(web));
    war.addAsWebInfResource(new StringAsset(portlet), "portlet.xml");

    //
    return war;
  }

  public static WebArchive createPortletDeployment(String packageName, URL portletXML) {
    WebArchive war = createDeployment(true, packageName);
    war.setWebXML(JuzuPortlet.class.getResource("web.xml"));
    war.addAsWebInfResource(new UrlAsset(portletXML), "portlet.xml");
    return war;
  }

  @ArquillianResource
  protected URL deploymentURL;

  /**
   * Returns the URL for portlet unit test.
   *
   * @return the base portlet URL
   */
  public URL getPortletURL() {
    try {
      return deploymentURL.toURI().resolve("embed/JuzuPortlet").toURL();
    }
    catch (Exception e) {
      throw failure(e);
    }
  }

  /**
   * Returns the URL for servlet unit test.
   *
   * @return the base servlet URL
   */
  public URL getServletURL() {
    return getServletURL("");
  }

  /**
   * Returns the URL for servlet unit test and the specified path.
   *
   * @return the base servlet URL
   */
  public URL getServletURL(String path) {

    // Remove any leading /
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    //
    URI base;
    try {
      URI uri = deploymentURL.toURI();

      //
      uri = uri.resolve(AbstractWebTestCase.path);

      //
      if (asDefault) {
        base = uri;
      } else {
        base = uri.resolve(getApplicationName().getIdentifier());
      }
      base = base.resolve(path);
      return base.toURL();
    }
    catch (Exception e) {
      throw failure("Could not build application url " + path, e);
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
    if (servlet) {
      return getServletURL();
    } else {
      return getPortletURL();
    }
  }

  public URL applicationURL(String path) {
    if (servlet) {
      return getServletURL(path);
    } else {
      throw failure("Cannot invoke portlet test with a path: " + path);
    }
  }
}
