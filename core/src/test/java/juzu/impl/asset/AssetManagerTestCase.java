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
package juzu.impl.asset;

import juzu.asset.AssetLocation;
import juzu.impl.common.Tools;
import juzu.impl.resource.ResourceResolver;
import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author Julien Viet
 */
public class AssetManagerTestCase extends AbstractTestCase {

  /** Dummy URL. */
  final static URL DUMMY_URL = AssetManagerTestCase.class.getResource(AssetManagerTestCase.class.getSimpleName());

  @Test
  public void testCycle1() {
    AssetManager mgr = new AssetManager("", ResourceResolver.NULL_RESOLVER);
    assertTrue(mgr.createDeployment().addAsset("foo", "script", AssetLocation.APPLICATION, "foo.js", null, null, DUMMY_URL, "bar").deploy());
    assertFalse(mgr.createDeployment().addAsset("bar", "script", AssetLocation.APPLICATION, "bar.js", null, null, DUMMY_URL, "foo").deploy());
  }

  @Test
  public void testCycle2() {
    AssetManager mgr = new AssetManager("", ResourceResolver.NULL_RESOLVER);
    assertTrue(mgr.createDeployment().addAsset("foo", "script", AssetLocation.APPLICATION, "foo.js", null, null, DUMMY_URL, "bar").deploy());
    assertTrue(mgr.createDeployment().addAsset("bar", "script", AssetLocation.APPLICATION, "bar.js", null, null, DUMMY_URL, "juu").deploy());
    assertFalse(mgr.createDeployment().addAsset("juu", "script", AssetLocation.APPLICATION, "juu.js", null, null, DUMMY_URL, "foo").deploy());
  }

  @Test
  public void testUndeploy() {
    AssetManager mgr = new AssetManager("", ResourceResolver.NULL_RESOLVER);
    AssetDeployment fooDepl = mgr.createDeployment();
    fooDepl.addAsset("foo", "script", AssetLocation.APPLICATION, "foo.js", null, null, DUMMY_URL, "bar");
    fooDepl.deploy();
    AssetDeployment barDepl = mgr.createDeployment();
    barDepl.addAsset("bar", "script", AssetLocation.APPLICATION, "bar.js", null, null, DUMMY_URL);
    barDepl.deploy();
    List<Asset> asset = Tools.list(mgr.resolveAssets(Collections.singletonList("foo")));
    assertEquals(2, asset.size());
    assertEquals("bar", asset.get(0).getId());
    assertEquals("foo", asset.get(1).getId());
    barDepl.undeploy();
    try {
      mgr.resolveAssets(Collections.singletonList("foo"));
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }
}
