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

package juzu.impl.plugin.application.descriptor;

import juzu.impl.common.JSON;
import juzu.impl.common.Name;
import juzu.impl.plugin.PluginDescriptor;

import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationModuleDescriptor extends PluginDescriptor {

  /** . */
  private LinkedHashSet<Name> names;

  public ApplicationModuleDescriptor(JSON json) {
    LinkedHashSet<Name> names = new LinkedHashSet<Name>();
    for (String name : json.names()) {
      names.add(Name.parse(name));
    }

    //
    this.names = names;
  }

  public Set<Name> getNames() {
    return names;
  }
}
