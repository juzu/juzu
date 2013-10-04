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

import juzu.impl.common.Path;
import juzu.impl.plugin.template.TemplatePlugin;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.tags.SimpleTag;
import juzu.impl.template.spi.TemplateProvider;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import java.io.IOException;
import java.io.Writer;

/** @author Julien Viet */
class TagEmitter extends AbstractEmitter {

  TagEmitter(AbstractContainerMetaModel owner) {
    super(owner);
  }

  @Override
  protected void emitClass(TemplateProvider<?> provider, TemplateMetaModel template, Element[] elements, Writer writer) throws IOException {

    //
    Path.Absolute path = template.getPath();

    // Template qualified class
    writer.append("package ").append(path.getDirs()).append(";\n");
    writer.append("import ").append(TemplateDescriptor.class.getCanonicalName()).append(";\n");
    writer.append("import ").append(TemplatePlugin.class.getCanonicalName()).append(";\n");
    writer.append("@").append(Generated.class.getName()).append("({})\n");
    writer.append("public class ").append(path.getRawName()).append(" extends ").append(SimpleTag.class.getName()).append("\n");
    writer.append("{\n");
    writer.append("public ").append(path.getRawName()).append("()\n");
    writer.append("{\n");
    writer.append("super(\"").
        append(path.getRawName()).append("\",").
        append("").append(provider.getTemplateStubType().getName()).append(".class").
        append(");\n");
    writer.append("}\n");

    // Close class
    writer.append("}\n");
  }
}
