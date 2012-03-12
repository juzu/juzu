package org.juzu.impl.plugin.ajax;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.plugin.ajax.Ajax;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   private final HashMap<ElementHandle.Package, AtomicBoolean> enabledMap = new HashMap<ElementHandle.Package, AtomicBoolean>();
   
   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values)
   {
      if (fqn.equals(Ajax.class.getName()))
      {
         ElementHandle.Package handle = application.getHandle();
         AtomicBoolean enabled = enabledMap.get(handle);
         enabled.set(true);
      }
   }

   @Override
   public void postConstruct(ApplicationMetaModel application)
   {
      enabledMap.put(application.getHandle(), new AtomicBoolean(false));
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      enabledMap.remove(application.getHandle());
   }

   @Override
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      ElementHandle.Package handle = application.getHandle();
      AtomicBoolean enabled = enabledMap.get(handle);
      return enabled != null && enabled.get() ? new JSON() : null;
   }
}
