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
package juzu.impl.plugin.template.metamodel;

import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;

import javax.lang.model.element.Element;

/** @author Julien Viet */
public class TemplateContainerMetaModel extends AbstractContainerMetaModel {

  /** . */
  public final static Key<TemplateContainerMetaModel> KEY = Key.of(TemplateContainerMetaModel.class);

  /** . */
  private static final Name NAME = Name.parse("templates");

  public TemplateContainerMetaModel() {
    super(NAME);
  }

  public void remove(ElementHandle.Field handle) {
    removeChild(Key.of(handle, TemplateRefMetaModel.class));
  }

  public TemplateRefMetaModel add(ElementHandle.Field handle, Path.Absolute path) {
    TemplateRefMetaModel ref = addChild(Key.of(handle, TemplateRefMetaModel.class), new ElementMetaModel(handle, path));
    if (qn.isPrefix(path.getName())) {
      add(path, ref);
    } else {
      // It's an unmanaged template by this container
    }
    return ref;
  }

  @Override
  protected Element[] getElements(TemplateMetaModel template) {
    return template.getReferencingElements();
  }

  @Override
  protected AbstractEmitter createEmitter() {
    return new TemplateEmitter(this);
  }
}
