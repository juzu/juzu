package org.juzu.plugin.less.impl;

import org.juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.plugin.less.Less;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPlugin extends Plugin
{
   public LessPlugin()
   {
      super("compiler");
   }

   @Override
   public Set<Class<? extends Annotation>> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>>singleton(Less.class);
   }

   @Override
   public ApplicationMetaModelPlugin newApplicationMetaModelPlugin()
   {
      return new LessMetaModelPlugin();
   }
}
