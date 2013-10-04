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
import juzu.impl.metamodel.Key;
import juzu.impl.tags.SimpleTag;
import juzu.template.TagHandler;

import javax.lang.model.element.Element;

/** @author Julien Viet */
class TagContainerMetaModel extends AbstractContainerMetaModel {

  /** . */
  static final Name NAME = Name.parse("tags");

  /** . */
  public final static Key<TagContainerMetaModel> KEY = Key.of(TagContainerMetaModel.class);

  TagContainerMetaModel() {
    super(NAME);
  }

  TagHandler resolveApplicationTagHandler(String name) {
    TagMetaModel tag = getChild(Key.of(name, TagMetaModel.class));
    if (tag != null) {
      TemplateMetaModel template = tag.getChild(TemplateMetaModel.KEY);
      Path.Absolute path = template.getPath();
      return new SimpleTag(name, path.getName().toString());
    }
    return null;
  }

  public TemplateMetaModel add(String name, Path.Relative path) {
    TemplateMetaModel template = add(path);
    TagMetaModel ref = addChild(Key.of(name, TagMetaModel.class), new TagMetaModel(name));
    ref.addChild(TemplateMetaModel.KEY, template);
    return template;
  }

  @Override
  protected Element[] getElements(TemplateMetaModel template) {
    return new Element[]{application.getProcessingContext().get(application.getHandle())};
  }

  @Override
  protected AbstractEmitter createEmitter() {
    return new TagEmitter(this);
  }
}
