package org.juzu.application;

import org.juzu.Action;
import org.juzu.Render;
import org.juzu.Resource;

import java.lang.annotation.Annotation;

/**
 * A phase.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum Phase
{

   /**
    * Action phase.
    */
   ACTION(Action.class),

   /**
    * Render phase.
    */
   RENDER(Render.class),

   /**
    * Resource phase.
    */
   RESOURCE(Resource.class);

   /** . */
   public final Class<? extends Annotation> annotation;

   Phase(Class<? extends Annotation> annotation)
   {
      this.annotation = annotation;
   }
}
