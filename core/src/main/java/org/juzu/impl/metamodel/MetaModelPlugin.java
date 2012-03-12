package org.juzu.impl.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.utils.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.util.Map;

/**
 * A plugin for meta model processing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MetaModelPlugin implements Serializable
{
   
   public void init(ApplicationsMetaModel applications)
   {
   }

   public void postActivateApplicationsMetaModel(ApplicationsMetaModel applications)
   {
   }

   public void postActivate(ApplicationMetaModel application)
   {
   }

   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values)
   {
   }

   public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event)
   {
   }

   public void postProcess(ApplicationMetaModel application)
   {
   }

   public void prePassivate(ApplicationMetaModel model)
   {
   }

   public void prePassivate(ApplicationsMetaModel applications)
   {
   }

   public void postConstruct(ApplicationMetaModel application)
   {
   }

   public void preDestroy(ApplicationMetaModel application)
   {
   }

   /**
    * Returns the plugin descriptor for the specified application or null if the plugin should not
    * be involved at runtime.
    *
    * @param application the application
    * @return the descriptor
    */
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      return null;
   }
}
