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
