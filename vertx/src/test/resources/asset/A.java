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

package asset;

import juzu.Response;
import juzu.View;
import juzu.asset.AssetLocation;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.ajax.AjaxPlugin;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;

public class A {

  @Inject
  AssetManager manager;

  @PostConstruct
  public void start() {
    URL url = AjaxPlugin.class.getClassLoader().getResource("juzu/impl/plugin/ajax/script.js");
    manager.addAsset(
        new AssetMetaData(
            "juzu.ajax",
            AssetLocation.APPLICATION,
            "/asset/juzu/impl/plugin/ajax/script.js",
            "jquery"),
        url);
  }

  @View
  public Response.Content index() throws Exception {
   return Response.ok("HELLO");
  }
}