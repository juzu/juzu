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

@Application
@Portlet
@Servlet("/weather8/*")
@Assets(
  {
    @Asset(id = "jquery", value = "jquery-1.7.1.min.js"),
    @Asset(id = "transition", value = "bootstrap-transition.js", depends = "jquery"),
    @Asset(id = "collapse", value = "bootstrap-collapse.js", depends = {"jquery", "transition"}),
    @Asset(value = "weather.js", depends = {"jquery", "collapse"}),
    @Asset("/examples/tutorial/assets/bootstrap.css")
  }
)
@WithAssets
package examples.tutorial.weather8;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.servlet.Servlet;