package org.juzu.impl.plugin;

import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.JSON;

/**
 * Base class for a plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Plugin
{

   /** The plugin name. */
   private final String name;

   protected Plugin(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   /**
    * Returns the meta model plugin type.
    *
    * @return the meta model plugin type
    */
   public abstract MetaModelPlugin newMetaModelPlugin();

   /**
    * Returns the plugin descriptor.
    *
    * @return the descriptor
    * @param loader the loader
    * @param config the plugin config
    * @throws Exception any exception
    */
   public abstract Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception;

}
