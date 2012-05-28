package org.juzu.impl.application;

/**
 * Scope something to be global or per application. Need to find a better name than just "scope".
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum Scope
{

   /**
    * The globally shared scope.
    */
   SHARED,

   /**
    * The scope of an application.
    */
   APPLICATION

}
