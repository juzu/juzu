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
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.common.Logger;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.template.TagHandler;

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
abstract class AbstractEmitter implements Serializable {

  /** . */
  private static final Logger log = BaseProcessor.getLogger(AbstractEmitter.class);

  /** . */
  final AbstractContainerMetaModel owner;

  /** . */
  private Set<Path.Absolute> emitted;

  /** . */
  private Map<Path.Absolute, FileObject> classCache;

  AbstractEmitter(AbstractContainerMetaModel owner) {
    this.owner = owner;
    this.emitted = new HashSet<Path.Absolute>();
    this.classCache = new HashMap<Path.Absolute, FileObject>();
  }

  void prePassivate() {
    log.info("Evicting cache " + emitted);
    emitted.clear();
    classCache.clear();
  }

  void emit(TemplateMetaModel template, Element[] elements) {
    TemplateProvider<?> provider = owner.resolveTemplateProvider(template.getPath().getExt());
    resolvedQualified(provider, template, elements);
    emitScript(template, provider, elements);
  }

  private void emitScript(final TemplateMetaModel template, final TemplateProvider provider, final Element[] elements) {
    owner.application.getProcessingContext().executeWithin(elements[0], new Callable<Void>() {
      public Void call() throws Exception {

        //
        Path.Absolute path = template.getPath();

        // If it's the cache we do nothing
        if (!emitted.contains(path)) {
          //
          try {
            EmitContext emitCtx = new EmitContext() {

              @Override
              public TagHandler resolveTagHandler(String name) {
                return owner.resolveTagHandler(name);
              }

              @Override
              public void createResource(Path.Absolute path, CharSequence content) throws IOException {
                FileKey key = FileKey.newName(path);
                FileObject scriptFile = owner.application.getProcessingContext().createResource(StandardLocation.CLASS_OUTPUT, key, elements);
                Writer writer = null;
                try {
                  writer = scriptFile.openWriter();
                  writer.append(content);
                  log.info("Generated template script " + path.getCanonical() + " as " + scriptFile.toUri() +
                      " with originating elements " + Arrays.asList(elements));
                }
                finally {
                  Tools.safeClose(writer);
                }
              }
            };

            //
            provider.emit(emitCtx, template.template);

            // Put it in cache
            emitted.add(path);
          }
          catch (Exception e) {
            throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_SCRIPT.failure(e, template.getPath());
          }
        }
        else {
          log.info("Template " + template.getPath() + " was found in cache");
        }
        return null;
      }
    });
  }

  protected abstract void emitClass(
      TemplateProvider<?> provider,
      TemplateMetaModel template,
      Element[] elements,
      Writer writer) throws IOException;

  private void resolvedQualified(
      TemplateProvider<?> provider,
      TemplateMetaModel template,
      Element[] elements) {

    //
    Path.Absolute path = template.getPath();
    if (classCache.containsKey(path)) {
      log.info("Template class " + path + " was found in cache");
    } else {
      Path.Absolute resolvedPath = owner.resolvePath(path);
      Writer writer = null;
      try {
        FileObject classFile = owner.application.getProcessingContext().createSourceFile(resolvedPath.getName(), elements);
        writer = classFile.openWriter();
        emitClass(provider, template, elements, writer);
        classCache.put(path, classFile);
        log.info("Generated template class " + path + " as " + classFile.toUri() +
            " with originating elements " + Arrays.asList(elements));
      }
      catch (IOException e) {
        e.printStackTrace();
        throw TemplateMetaModel.CANNOT_WRITE_TEMPLATE_CLASS.failure(e, elements[0], path);
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
