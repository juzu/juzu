package org.juzu;

/**
 * Denoates something ambiguous that could not be resolved.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AmbiguousResolutionException extends RuntimeException
{

   public AmbiguousResolutionException()
   {
   }

   public AmbiguousResolutionException(String message)
   {
      super(message);
   }

   public AmbiguousResolutionException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public AmbiguousResolutionException(Throwable cause)
   {
      super(cause);
   }
}
