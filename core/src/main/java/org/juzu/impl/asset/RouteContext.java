package org.juzu.impl.asset;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RouteContext
{

   /**
    * Render the URL context in the provided output.
    *
    * @param out where chars should go
    * @throws java.io.IOException any io exception thrown by the output
    */
   public abstract void renderURL(Appendable out) throws IOException;


}
