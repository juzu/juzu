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

package juzu.impl.plugin.module.metamodel;

import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.metamodel.AnnotationChange;
import juzu.impl.metamodel.MetaModelPlugin;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ModuleMetaModelPlugin extends MetaModelPlugin<ModuleMetaModel, ModuleMetaModelPlugin> {

  public ModuleMetaModelPlugin(String name) {
    super(name);
  }

  @Override
  public void processAnnotationChange(ModuleMetaModel metaModel, AnnotationChange change) {
    ElementHandle<?> elt = change.getKey().getElement();
    if (elt instanceof ElementHandle.Package) {
      ElementHandle.Package pkgElt = (ElementHandle.Package)elt;
      DiskFileSystem fs = metaModel.getProcessingContext().getSourcePath(pkgElt);
      if (fs != null) {
        metaModel.root = fs.getRoot();
      }
    }
    super.processAnnotationChange(metaModel, change);
  }

  /**
   * Returns a JSON representation mainly for testing purposes.
   *
   * @param metaModel the meta model instance
   * @return the json representation
   */
  public JSON toJSON(ModuleMetaModel metaModel) {
    return null;
  }
}
