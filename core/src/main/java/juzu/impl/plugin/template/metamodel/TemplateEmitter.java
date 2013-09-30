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

import juzu.impl.common.FileKey;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.Template;
import juzu.impl.plugin.template.TemplatePlugin;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.common.Logger;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The template emitter.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TemplateEmitter implements Serializable {

  /** . */
  private static final Logger log = BaseProcessor.getLogger(TemplateEmitter.class);

  /** . */
  final TemplatesMetaModel owner;

  /** . */
  private Set<Path.Relative> emitted;

  /** . */
  private Map<Path.Relative, FileObject> classCache;

  TemplateEmitter(TemplatesMetaModel owner) {
    this.owner = owner;
    this.emitted = new HashSet<Path.Relative>();
    this.classCache = new HashMap<Path.Relative, FileObject>();
  }

  void prePassivate() {
    log.log("Evicting cache " + emitted);
    emitted.clear();
    classCache.clear();
  }

  void process(TemplateMetaModelPlugin plugin) throws ProcessingException {

    // Generate missing files from template
    for (TemplateMetaModel templateMM : owner.getChildren(TemplateMetaModel.class)) {

      //
      Template<?> template = templateMM.template;

      // We compute the class elements from the field elements (as eclipse will make the relationship)
      Element[] elements = templateMM.getReferencingElements();

      // If CCE that would mean there is an internal bug
      TemplateProvider<?> provider = (TemplateProvider<?>)plugin.providers.get(template.getRelativePath().getExt());

      // Resolve the qualified class
      resolvedQualified(provider, template, elements);

      //
      resolveScript(template, plugin, elements);
    }
  }

  private <M extends Serializable> void resolveScript(final Template<M> template, final TemplateMetaModelPlugin plugin, final Element[] elements) {
    owner.application.getProcessingContext().executeWithin(elements[0], new Callable<Void>() {
      public Void call() throws Exception {

        // If CCE that would mean there is an internal bug
        TemplateProvider<M> provider = (TemplateProvider<M>)plugin.providers.get(template.getRelativePath().getExt());

        //
        Path.Relative path = template.getRelativePath();

        // If it's the cache we do nothing
        if (!emitted.contains(path)) {
          //
          try {
            EmitContext emitCtx = new EmitContext() {
              public void createResource(String rawName, String ext, CharSequence content) throws IOException {
                Path.Relative bar = template.getRelativePath().as(rawName, ext);
                Path.Absolute absolute = owner.resolvePath(bar);
                FileKey key = FileKey.newName(absolute);
                FileObject scriptFile = owner.application.getProcessingContext().createResource(StandardLocation.CLASS_OUTPUT, key, elements);
                Writer writer = null;
                try {
                  writer = scriptFile.openWriter();
                  writer.append(content);
                  log.log("Generated template script " + bar + " as " + scriptFile.toUri() +
                      " with originating elements " + Arrays.asList(elements));
                }
                finally {
                  Tools.safeClose(writer);
                }
              }
            };

            //
            provider.emit(emitCtx, template);

            // Put it in cache
            emitted.add(path);
          }
          catch (Exception e) {
            throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_SCRIPT.failure(e, template.getRelativePath());
          }
        }
        else {
          log.log("Template " + template.getRelativePath() + " was found in cache");
        }
        return null;
      }
    });
  }

  private <M extends Serializable> void resolvedQualified(
      TemplateProvider<?> provider,
      Template<M> template,
      Element[] elements) {

    //
    Path.Relative path = template.getRelativePath();
    if (classCache.containsKey(path)) {
      log.log("Template class " + path + " was found in cache");
      return;
    }

    //
    Path.Absolute resolvedPath = owner.resolvePath(path);

    //
    Writer writer = null;
    try {
      // Template qualified class
      FileObject classFile = owner.application.getProcessingContext().createSourceFile(resolvedPath.getName(), elements);
      writer = classFile.openWriter();
      writer.append("package ").append(resolvedPath.getDirs()).append(";\n");
      writer.append("import ").append(TemplateDescriptor.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(TemplatePlugin.class.getCanonicalName()).append(";\n");
      writer.append("@").append(Generated.class.getName()).append("({})\n");
      writer.append("@").append(juzu.Path.class.getName()).append("(\"").append(path.getValue()).append("\")\n");
      writer.append("public class ").append(path.getRawName()).append(" extends ").append(juzu.template.Template.class.getName()).append("\n");
      writer.append("{\n");
      writer.append("@javax.inject.Inject\n");
      writer.append("public ").append(path.getRawName()).append("(").
        append(TemplatePlugin.class.getSimpleName()).append(" templatePlugin").
        append(")\n");
      writer.append("{\n");
      writer.append("super(templatePlugin, \"").append(path.getValue()).append("\"").append(", ").append(provider.getTemplateStubType().getName()).append(".class);\n");
      writer.append("}\n");

      //
      writer.
          append("public static final ").append(TemplateDescriptor.class.getName()).append(" DESCRIPTOR = new ").append(TemplateDescriptor.class.getName()).append("(").
          append(resolvedPath.getName()).append(".class,").
          append(provider.getTemplateStubType().getName()).append(".class").
          append(");\n");

      //
      String baseBuilderName = juzu.template.Template.Builder.class.getCanonicalName();
      if (template.getParameters() != null) {
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
        for (String paramName : template.getParameters()) {
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

      //
      classCache.put(path, classFile);

      //
      log.log("Generated template class " + path + " as " + classFile.toUri() +
        " with originating elements " + Arrays.asList(elements));
    }
    catch (IOException e) {
      throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_CLASS.failure(e, elements[0], path);
    }
    finally {
      Tools.safeClose(writer);
    }
  }
}
