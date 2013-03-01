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

package juzu.impl.plugin.asset;

import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.test.CompilerAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationRelativeTestCase extends AbstractLocationTestCase {
  @Deployment
  public static WebArchive createDeployment() {
    return createLocationDeployment("plugin.asset.location.applicationrelative");
  }

  @Test
  @RunAsClient
  public final void testCopyAsset() throws Exception {
    CompilerAssert<File, File> compiler = getCompiler();
    ReadWriteFileSystem<File> classOutput = compiler.getClassOutput();
    File file = classOutput.getPath("plugin", "asset", "location", "applicationrelative", "assets", "test.js");
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.isFile());
  }
}
