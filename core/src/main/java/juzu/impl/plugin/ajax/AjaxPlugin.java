package juzu.impl.plugin.ajax;

import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.Plugin;
import juzu.impl.request.RequestLifeCycle;
import juzu.plugin.ajax.Ajax;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxPlugin extends Plugin
{
   public AjaxPlugin()
   {
      super("ajax");
   }

   @Override
   public Set<Class<? extends Annotation>> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>>singleton(Ajax.class);
   }

   @Override
   public Class<? extends RequestLifeCycle> getLifeCycleClass()
   {
      return AjaxLifeCycle.class;
   }

   @Override
   public ApplicationMetaModelPlugin newApplicationMetaModelPlugin()
   {
      return new AjaxMetaModelPlugin();
   }
}
