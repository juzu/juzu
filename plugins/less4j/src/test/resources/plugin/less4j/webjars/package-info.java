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
@WebJars(@WebJar("bootstrap"))
@Less(@Asset("bootstrap/3.1.1/less/bootstrap.less"))
package plugin.less4j.webjars;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.less4j.Less;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.webjars.WebJar;