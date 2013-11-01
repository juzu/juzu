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

package juzu.plugin.less4j.impl;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import juzu.impl.common.Name;
import juzu.impl.compiler.Message;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Logger;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.plugin.less4j.Less;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelPluginImpl extends ModuleMetaModelPlugin {

  /** . */
  public static final MessageCode GENERAL_PROBLEM = new MessageCode(
      "GENERAL_PROBLEM",
      "%1$s:\n%2$s");

  /** . */
  public static final MessageCode COMPILATION_ERROR = new MessageCode(
    "LESS_COMPILATION_ERROR",
    "%1$s in %2$s on line %3$s:\n%4$s");

  /** . */
  public static final MessageCode MALFORMED_PATH = new MessageCode("LESS_MALFORMED_PATH", "The resource path %1$s is malformed");

  /** . */
  static final Logger log = BaseProcessor.getLogger(MetaModelPluginImpl.class);

  /** . */
  private HashMap<Name, AnnotationState> annotations;

  public MetaModelPluginImpl() {
    super("less");
  }

  @Override
  public void init(ModuleMetaModel metaModel) {
    annotations = new HashMap<Name, AnnotationState>();
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Less.class);
  }

  @Override
  public void processAnnotationAdded(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    Name pkg = key.getElement().getPackageName();
    log.info("Adding less annotation for package " + pkg);
    annotations.put(pkg, added);
  }

  @Override
  public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    Name pkg = key.getElement().getPackageName();
    log.info("Removing less annotation for package " + pkg);
    annotations.remove(pkg);
  }

  @Override
  public void postActivate(ModuleMetaModel metaModel) {
    annotations = new HashMap<Name, AnnotationState>();
  }

  @Override
  public void prePassivate(ModuleMetaModel metaModel) {
    // First clear annotation map
    HashMap<Name, AnnotationState> clone = annotations;
    annotations = null;

    //
    for (Map.Entry<Name, AnnotationState> entry : clone.entrySet()) {
      AnnotationState annotation = entry.getValue();
      Name pkg = entry.getKey();
      ProcessingContext env = metaModel.processingContext;
      ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
      PackageElement pkgElt = env.get(pkgHandle);
      Boolean minify = (Boolean)annotation.get("minify");
      List<String> resources = (List<String>)annotation.get("value");

      // WARNING THIS IS NOT CORRECT BUT WORK FOR NOW
      AnnotationMirror annotationMirror = Tools.getAnnotation(pkgElt, Less.class.getName());

      //
      log.info("Handling less annotation for package " + pkg + ": minify=" + minify + " resources=" + resources);

      //
      if (resources != null && resources.size() > 0) {

        // For now we use the hardcoded assets package
        Name assetPkg = pkg.append("assets");

        //
        for (String resource : resources) {
          log.info("Processing declared resource " + resource);

          //
          Path path;
          try {
            path = Path.parse(resource);
          }
          catch (IllegalArgumentException e) {
            throw MALFORMED_PATH.failure(pkgElt, annotationMirror, resource).initCause(e);
          }

          //
          Path.Absolute to = assetPkg.resolve(path.as("css"));
          log.info("Resource " + resource + " destination resolved to " + to);

          //
          CompilerLessSource file = new CompilerLessSource(env, pkgHandle, assetPkg.resolve(path));

          //
          LessCompiler compiler = new ThreadUnsafeLessCompiler();
          LessCompiler.CompilationResult result;
          try {
            result = compiler.compile(file);
          }
          catch (Less4jException e) {
            List<LessCompiler.Problem> errors = e.getErrors();
            ArrayList<Message> messages = new ArrayList<Message>(errors.size());
            for (LessCompiler.Problem error : errors) {
              String text = error.getMessage() != null ? error.getMessage() : "There is an error in your .less file";
              String errorName = error.getType().name();
              LessSource source = error.getSource();
              Message msg;
              if (source != null) {
                msg = new Message(COMPILATION_ERROR, errorName, source.getName(), error.getLine(), text);
              } else {
                msg = new Message(GENERAL_PROBLEM, errorName, text);
              }
              log.info(msg.toDisplayString());
              messages.add(msg);
            }
            throw new ProcessingException(pkgElt, annotationMirror, messages);
          }

          //
          try {
            log.info("Resource " + resource + " compiled about to write on disk as " + to);
            FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, to);
            Writer writer = fo.openWriter();
            try {
              writer.write(result.getCss());
            }
            finally {
              Tools.safeClose(writer);
            }
          }
          catch (IOException e) {
            log.info("Resource " + to + " could not be written on disk", e);
          }
        }
      }
    }
  }
}
