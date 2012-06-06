package juzu.plugin.less.impl;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.utils.Content;
import juzu.impl.utils.Path;
import juzu.impl.utils.QN;
import juzu.plugin.less.impl.lesser.LessContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CompilerLessContext implements LessContext
{

   /** . */
   final ProcessingContext processingContext;

   /** . */
   final QN pkg;

   CompilerLessContext(ProcessingContext processingContext, QN pkg)
   {
      this.processingContext = processingContext;
      this.pkg = pkg;
   }

   public String load(String ref)
   {

      ElementHandle.Package pkgElt = ElementHandle.Package.create(pkg);

      try
      {
         Path path = Path.parse(ref);
         Path.Absolute resolvable = Path.Absolute.create(pkg.append(path.getQN()), path.getRawName(), path.getExt());
         Content c = processingContext.resolveResource(pkgElt, resolvable);
         if (c != null)
         {
            return c.getCharSequence().toString();
         }
      }
      catch (IllegalArgumentException e)
      {
         // Log ?
      }

      //
      return null;
   }
}
