package org.juzu.impl.controller;

import org.juzu.Action;
import org.juzu.Resource;
import org.juzu.View;
import org.juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import org.juzu.impl.controller.descriptor.ControllerDescriptor;
import org.juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;

import java.lang.annotation.Annotation;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin
{

   public ControllerPlugin()
   {
      super("controller");
   }

   @Override
   public Set<Class<? extends Annotation>> getAnnotationTypes()
   {
      return Tools.<Class<? extends Annotation>>set(View.class, Action.class, Resource.class);
   }

   @Override
   public ApplicationMetaModelPlugin newApplicationMetaModelPlugin()
   {
      return new ControllerMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new ControllerDescriptor(loader, config);
   }
}
