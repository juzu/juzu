package juzu.impl.inject;

/**
 * Wrap a scoped value.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface Scoped
{

   /**
    * Returns the scoped value.
    *
    * @return the value
    */
   Object get();

   /**
    * Signals the scoped value is destroyed.
    */
   void destroy();
}
