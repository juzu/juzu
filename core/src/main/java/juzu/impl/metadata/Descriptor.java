package juzu.impl.metadata;

import java.util.Collections;

/**
 * Base descriptor class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Descriptor
{

   /**
    * Returns the associated plugin class.
    *
    * @return the list of bean ot install
    */
   public Iterable<BeanDescriptor> getBeans()
   {
      return Collections.emptyList();
   }
}
