package org.juzu.impl.plugin;

import org.juzu.impl.application.Scope;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.utils.JSON;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

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

   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Collections.emptyMap();
   }

   /**
    * Returns the meta model plugin type.
    *
    * @return the meta model plugin type
    */
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new MetaModelPlugin();
   }

   /**
    * Returns the plugin descriptor.
    *
    * @return the descriptor
    * @param loader the loader
    * @param config the plugin config
    * @throws Exception any exception
    */
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new Descriptor()
      {
         @Override
         public Iterable<BeanDescriptor> getBeans()
         {
            Class<? extends RequestLifeCycle> lifeCycleClass = getLifeCycleClass();
            if (lifeCycleClass != null)
            {
               return Collections.singletonList(new BeanDescriptor(lifeCycleClass, null, null, null));
            }
            else
            {
               return Collections.emptyList();
            }
         }
      };
   }
   
   public Class<? extends RequestLifeCycle> getLifeCycleClass()
   {
      return null;
   }

}
