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

package juzu.processor;

import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.metamodel.MetaModelProcessor;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MainProcessor extends MetaModelProcessor {

  @Override
  protected Class<? extends MetaModelPlugin<?, ?>> getPluginType() {
    return ModuleMetaModelPlugin.class;
  }

  @Override
  protected MetaModel<?, ?> createMetaModel() {
    return new ModuleMetaModel();
  }
}
