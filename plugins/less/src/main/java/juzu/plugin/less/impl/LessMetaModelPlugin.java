/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.plugin.less.impl;

import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;
import juzu.impl.common.FQN;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.Message;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Logger;
import juzu.impl.common.Path;
import juzu.impl.common.QN;
import juzu.impl.common.Tools;
import juzu.plugin.less.Less;
import juzu.plugin.less.impl.lesser.Compilation;
import juzu.plugin.less.impl.lesser.Failure;
import juzu.plugin.less.impl.lesser.JSR223Context;
import juzu.plugin.less.impl.lesser.LessError;
import juzu.plugin.less.impl.lesser.Lesser;
import juzu.plugin.less.impl.lesser.Result;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessMetaModelPlugin extends ModuleMetaModelPlugin {

  /** . */
  public static final MessageCode COMPILATION_ERROR = new MessageCode(
    "LESS_COMPILATION_ERROR",
    "%1$s in %2$s on line %3$s, column %4$s:\n%5$s");

  /** . */
  public static final MessageCode MALFORMED_PATH = new MessageCode("LESS_MALFORMED_PATH", "The resource path %1$s is malformed");

  /** . */
  private static final FQN LESS = new FQN(Less.class);

  /** . */
  static final Logger log = BaseProcessor.getLogger(LessMetaModelPlugin.class);

  /** . */
  private HashMap<QN, AnnotationState> annotations;

  public LessMetaModelPlugin() {
    super("less");
  }

  @Override
  public void init(ModuleMetaModel metaModel) {
    annotations = new HashMap<QN, AnnotationState>();
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Less.class);
  }

  @Override
  public void processAnnotationChange(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (key.getType().equals(LESS)) {
      QN pkg = key.getElement().getPackage();
      log.log("Recording less annotation for package " + pkg);
      annotations.put(pkg, added);
    }
  }

  @Override
  public void postActivate(ModuleMetaModel metaModel) {
    annotations = new HashMap<QN, AnnotationState>();
  }

  @Override
  public void prePassivate(ModuleMetaModel metaModel) {
    // First clear annotation map
    HashMap<QN, AnnotationState> clone = annotations;
    annotations = null;

    //
    for (Map.Entry<QN, AnnotationState> entry : clone.entrySet()) {
      AnnotationState annotation = entry.getValue();
      QN pkg = entry.getKey();
      ProcessingContext env = metaModel.env;
      ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
      PackageElement pkgElt = env.get(pkgHandle);
      Boolean minify = (Boolean)annotation.get("minify");
      List<String> resources = (List<String>)annotation.get("value");

      // WARNING THIS IS NOT CORRECT BUT WORK FOR NOW
      AnnotationMirror annotationMirror = Tools.getAnnotation(pkgElt, Less.class.getName());

      //
      log.log("Handling less annotation for package " + pkg + ": minify=" + minify + " resources=" + resources);

      //
      if (resources != null && resources.size() > 0) {

        // For now we use the hardcoded assets package
        QN assetPkg = pkg.append("assets");

        //
        CompilerLessContext clc = new CompilerLessContext(env, pkgHandle, assetPkg);

        //
        for (String resource : resources) {
          log.log("Processing declared resource " + resource);

          //
          Path path;
          try {
            path = Path.parse(resource);
          }
          catch (IllegalArgumentException e) {
            throw MALFORMED_PATH.failure(pkgElt, annotationMirror, resource).initCause(e);
          }

          //
          Path.Absolute to = Path.Absolute.create(assetPkg.append(path.getQN()), path.getRawName(), "css");
          log.log("Resource " + resource + " destination resolved to " + to);

          //
          Lesser lesser;
          Result result;
          try {
            lesser = new Lesser(new JSR223Context());
            result = lesser.compile(clc, resource, Boolean.TRUE.equals(minify));
          }
          catch (Exception e) {
            log.log("Unexpected exception", e);
            throw new UnsupportedOperationException(e);
          }

          //
          if (result instanceof Compilation) {
            try {
              log.log("Resource " + resource + " compiled about to write on disk as " + to);
              Compilation compilation = (Compilation)result;
              FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, to);
              Writer writer = fo.openWriter();
              try {
                writer.write(compilation.getValue());
              }
              finally {
                Tools.safeClose(writer);
              }
            }
            catch (IOException e) {
              log.log("Resource " + to + " could not be written on disk", e);
            }
          }
          else {
            Failure failure = (Failure)result;
            LinkedList<LessError> errors = failure.getErrors();
            ArrayList<Message> messages = new ArrayList<Message>(errors.size());
            StringBuilder sb = new StringBuilder();
            for (LessError error : errors) {
              String text = error.message != null ? error.message : "There is an error in your .less file";
              int index = error.line - (error.extract.length - 1) / 2;
              for (String line : error.extract) {
                sb.append("[").append(index).append("]");
                sb.append(index == error.line ? " -> " : "    ");
                sb.append(line).append("\n");
                index++;
              }
              Message msg = new Message(COMPILATION_ERROR,
                text,
                error.src,
                error.line,
                error.column + 1,
                sb);
              log.log(msg.toDisplayString());
              messages.add(msg);
            }
            throw new ProcessingException(pkgElt, annotationMirror, messages);
          }
        }
      }
    }
  }
}
