package juzu.plugin.less.impl;

import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.CompilationMessage;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Path;
import juzu.impl.utils.QN;
import juzu.impl.utils.Tools;
import juzu.plugin.less.Less;
import juzu.plugin.less.impl.lesser.Compilation;
import juzu.plugin.less.impl.lesser.Failure;
import juzu.plugin.less.impl.lesser.JSR223Context;
import juzu.plugin.less.impl.lesser.LessError;
import juzu.plugin.less.impl.lesser.Lesser;
import juzu.plugin.less.impl.lesser.Result;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessMetaModelPlugin extends MetaModelPlugin {

  /** . */
  public static final MessageCode COMPILATION_ERROR = new MessageCode(
    "LESS_COMPILATION_ERROR",
    "%1$s in %2$s on line %3$s, column %4$s:\n%5$s");

  /** . */
  public static final MessageCode MALFORMED_PATH = new MessageCode("LESS_MALFORMED_PATH", "The resource path %1$s is malformed");

  /** . */
  static final Logger log = BaseProcessor.getLogger(LessMetaModelPlugin.class);

  /** . */
  private HashMap<ElementHandle.Package, AnnotationData> annotations;

  public LessMetaModelPlugin() {
    super("less");
  }

  @Override
  public void init(MetaModel metaModel) {
    annotations = new HashMap<ElementHandle.Package, AnnotationData>();
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.<Class<? extends Annotation>>singleton(Less.class);
  }

  @Override
  public void processAnnotation(MetaModel metaModel, Element element, String fqn, AnnotationData data) {
    if (fqn.equals(Less.class.getName())) {
      ElementHandle.Package pkg = (ElementHandle.Package)ElementHandle.create(element);
      log.log("Recording less annotation for package " + pkg.getQN());
      annotations.put(pkg, data);
    }
  }

  @Override
  public void postActivate(MetaModel metaModel) {
    annotations = new HashMap<ElementHandle.Package, AnnotationData>();
  }

  @Override
  public void prePassivate(MetaModel metaModel) {
    // First clear annotation map
    HashMap<ElementHandle.Package, AnnotationData> clone = annotations;
    annotations = null;

    //
    for (Map.Entry<ElementHandle.Package, AnnotationData> entry : clone.entrySet()) {
      AnnotationData annotation = entry.getValue();
      ElementHandle.Package pkg = entry.getKey();
      ProcessingContext env = metaModel.env;
      PackageElement pkgElt = env.get(pkg);
      Boolean minify = (Boolean)annotation.get("minify");
      List<String> resources = (List<String>)annotation.get("value");

      // WARNING THIS IS NOT CORRECT BUT WORK FOR NOW
      AnnotationMirror annotationMirror = Tools.getAnnotation(pkgElt, Less.class.getName());

      //
      log.log("Handling less annotation for package " + pkg.getQN() + ": minify=" + minify + " resources=" + resources);

      //
      if (resources != null && resources.size() > 0) {

        // For now we use the hardcoded assets package
        QN assetPkg = pkg.getQN().append("assets");

        //
        CompilerLessContext clc = new CompilerLessContext(env, entry.getKey(), assetPkg);

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
            ArrayList<CompilationMessage> messages = new ArrayList<CompilationMessage>(errors.size());
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
              CompilationMessage msg = new CompilationMessage(COMPILATION_ERROR,
                text,
                error.src,
                error.line,
                error.column + 1,
                sb);
              log.log(msg.toDisplayString());
              messages.add(msg);
            }
            throw new CompilationException(pkgElt, annotationMirror, messages);
          }
        }
      }
    }
  }
}
