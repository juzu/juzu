package org.juzu.plugin.less.impl;

import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.Path;
import org.juzu.impl.utils.QN;
import org.juzu.plugin.less.impl.lesser.LessContext;

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
