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
import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.fs.spi.ram.RAMPath;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractWebTestCase extends AbstractTestCase {

  public static WebArchive createDeployment(String... pkgName) {

    // Compile classes
    DiskFileSystem sourcePath = diskFS(pkgName);
    RAMFileSystem sourceOutput = new RAMFileSystem();
    RAMFileSystem classOutput = new RAMFileSystem();
    CompilerAssert<File, RAMPath> compiler = new CompilerAssert<File, RAMPath>(
        false,
        sourcePath,
        sourceOutput,
        classOutput);
    compiler.assertCompile();

    // Create war
    final WebArchive war = ShrinkWrap.create(WebArchive.class, "juzu.war");

    // Add output to war
    try {
      classOutput.traverse(new Visitor.Default<RAMPath>() {

        LinkedList<String> path = new LinkedList<String>();

        @Override
        public void enterDir(RAMPath dir, String name) throws IOException {
          path.addLast(name.isEmpty() ? "classes" : name);
        }

        @Override
        public void leaveDir(RAMPath dir, String name) throws IOException {
          path.removeLast();
        }

        @Override
        public void file(RAMPath file, String name) throws IOException {
          path.addLast(name);
          String target = Tools.join('/', path);
          path.removeLast();
          war.addAsWebInfResource(new ByteArrayAsset(file.getContent().getInputStream()), target);
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
