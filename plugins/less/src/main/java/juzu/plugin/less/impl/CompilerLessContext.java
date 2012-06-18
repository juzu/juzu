package juzu.plugin.less.impl;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.utils.Content;
import juzu.impl.utils.Path;
import juzu.impl.utils.QN;
import juzu.plugin.less.impl.lesser.LessContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CompilerLessContext implements LessContext {

  /** . */
  final ProcessingContext processingContext;

  /** . */
  final ElementHandle.Package context;

  /** . */
  final QN pkg;

  /** . */
  final Path.Absolute pkgPath;

  CompilerLessContext(
    ProcessingContext processingContext,
    ElementHandle.Package context,
    QN pkg ) {
    this.processingContext = processingContext;
    this.context = context;
    this.pkg = pkg;
    this.pkgPath = Path.Absolute.create(pkg, "", "");
  }

  public String load(String ref) {
    try {
      Path.Absolute path = pkgPath.append(ref);
      Content c = processingContext.resolveResource(context, path);
      if (c != null) {
        return c.getCharSequence().toString();
      }
    }
    catch (IllegalArgumentException e) {
      // Log ?
    }

    //
    return null;
  }
}
