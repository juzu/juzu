package org.juzu.impl.template;

import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.template.metadata.TemplatesDescriptor;
import org.juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import org.juzu.impl.utils.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatePlugin extends Plugin
{

   public TemplatePlugin()
   {
      super("template");
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new TemplateMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new TemplatesDescriptor(loader, config);
   }
}
