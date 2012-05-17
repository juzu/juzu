package org.juzu.impl.plugin.portlet;

import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.portlet.Portlet;

import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPlugin extends Plugin
{

   /** . */
   private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.unmodifiableSet(Tools.set(Portlet.class.getName()));

   public PortletPlugin()
   {
      super("portlet");
   }

   @Override
   public Set<String> getSupportedAnnotationTypes()
   {
      return SUPPORTED_ANNOTATIONS;
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new PortletMetaModelPlugin();
   }
}
