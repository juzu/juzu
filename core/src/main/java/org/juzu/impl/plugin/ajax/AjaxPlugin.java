package org.juzu.impl.plugin.ajax;

import org.juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.plugin.ajax.Ajax;

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
