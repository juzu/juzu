package org.juzu.impl.model.meta;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.utils.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.util.Map;

/**
 * A plugin for meta model processing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class MetaModelPlugin implements Serializable
{
   
   public void init(MetaModel model)
   {
   }

   public void postActivate(MetaModel moel)
   {

   }

   public void processAnnotation(
      MetaModel model,
      Element element,
      String annotationFQN,
      Map<String, Object> annotationValues) throws CompilationException
   {
   }

   public void processEvent(MetaModel model, MetaModelEvent event)
   {
   }

   public void postProcess(MetaModel model)
   {
   }

   public void prePassivate(MetaModel model)
   {

   }
   
   public void emitConfig(ApplicationMetaModel application, JSON json)
   {
   }
}
