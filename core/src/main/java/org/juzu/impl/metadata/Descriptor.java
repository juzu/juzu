package org.juzu.impl.metadata;

/**
 * Base metadata class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Descriptor
{

   /**
    * Returns the associated plugin class or null.
    *
    * @return the list of bean ot install
    */
   protected abstract Iterable<BeanDescriptor> getBeans();

}
