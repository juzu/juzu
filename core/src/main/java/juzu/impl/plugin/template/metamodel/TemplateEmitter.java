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
import juzu.impl.template.spi.TemplateProvider;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import java.io.Writer;
import java.io.IOException;

/** @author Julien Viet */
class TemplateEmitter extends AbstractEmitter {

  TemplateEmitter(AbstractContainerMetaModel owner) {
    super(owner);
  }

  @Override
  protected void emitClass(
      TemplateProvider provider,
      TemplateMetaModel template,
      Element[] elements,
      Writer writer) throws IOException {

    //
    Path.Absolute path = template.getPath();

    // Template qualified class
    writer.append("package ").append(path.getDirs()).append(";\n");
    writer.append("import ").append(TemplateDescriptor.class.getCanonicalName()).append(";\n");
    writer.append("import ").append(TemplatePlugin.class.getCanonicalName()).append(";\n");
    writer.append("@").append(Generated.class.getName()).append("({})\n");
    writer.append("public class ").append(path.getRawName()).append(" extends ").append(juzu.template.Template.class.getName()).append("\n");
    writer.append("{\n");
    writer.append("@javax.inject.Inject\n");
    writer.append("public ").append(path.getRawName()).append("(").
        append(TemplatePlugin.class.getSimpleName()).append(" templatePlugin").
        append(")\n");
    writer.append("{\n");
    writer.append("super(templatePlugin, \"").append(path.getValue()).append("\");\n");
    writer.append("}\n");

    //
    writer.
        append("public static final ").append(TemplateDescriptor.class.getName()).append(" DESCRIPTOR = new ").append(TemplateDescriptor.class.getName()).append("(").
        append("\"").append(path.getValue()).append("\",").
        append("0x").append(Long.toHexString(template.template.getMD5())).append("L,").
        append(path.getName()).append(".class,").
        append(provider.getTemplateStubType().getName()).append(".class").
        append(");\n");

    //
    String baseBuilderName = juzu.template.Template.Builder.class.getCanonicalName();
    if (template.template.getParameters() != null) {
      // Implement abstract method with this class Builder covariant return type
      writer.append("public Builder builder() {\n");
      writer.append("return new Builder();\n");
      writer.append("}\n");

      // Covariant return type of with()
      writer.append("public Builder with() {\n");
      writer.append("return (Builder)super.with();\n");
      writer.append("}\n");

      // Setters on builders
      writer.append("public class Builder extends ").append(baseBuilderName).append("\n");
      writer.append("{\n");
      for (String paramName : template.template.getParameters()) {
        writer.append("public Builder ").append(paramName).append("(Object ").append(paramName).append(") {\n");
        writer.append("set(\"").append(paramName).append("\",").append(paramName).append(");\n");
        writer.append("return this;\n");
        writer.append(("}\n"));
      }
      writer.append("}\n");
    }
    else {
      // Implement abstract factory method
      writer.append("public ").append(baseBuilderName).append(" builder() {\n");
      writer.append("return new ").append(baseBuilderName).append("();\n");
      writer.append("}\n");
    }

    // Close class
    writer.append("}\n");
  }
}
