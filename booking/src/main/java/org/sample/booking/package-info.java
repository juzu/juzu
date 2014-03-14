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

@Application(defaultController = org.sample.booking.controllers.Application.class)
@Bindings({@Binding(Flash.class), @Binding(Login.class)})
@Servlet("/")
@WebJars(@WebJar("jquery"))
@Stylesheets(
    {
        @Stylesheet("stylesheets/main.css"),
        @Stylesheet("ui-lightness/jquery-ui-1.7.2.custom.css")
    }
)
@Scripts(
    {
        @Script(id = "jquery", value = "jquery/${jquery.version}/jquery.min.js"),
        @Script(value = "javascripts/jquery-ui-1.7.2.custom.min.js", depends = "jquery"),
        @Script(value = "javascripts/booking.js", depends = "juzu.ajax")
    }
)
@Assets("*")
package org.sample.booking;

import juzu.Application;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.asset.Assets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.servlet.Servlet;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import org.sample.booking.controllers.Login;