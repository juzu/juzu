package org.juzu.impl.plugin.binding;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.plugin.binding.Bindings;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   private Map<ElementHandle.Package, JSON> state = new HashMap<ElementHandle.Package, JSON>();

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values)
   {
      if (fqn.equals(Bindings.class.getName()))
      {
         List<Map<String, Object>> bindings = (List<Map<String, Object>>)values.get("value");
         ArrayList<JSON> list = new ArrayList<JSON>();
         if (bindings != null)
         {
            for (Map<String, Object> binding : bindings)
            {
               ElementHandle.Class bindingValue = (ElementHandle.Class)binding.get("value");
               ElementHandle.Class bindingImplementation = (ElementHandle.Class)binding.get("implementation");
               String scope = (String)binding.get("scope");
               JSON a = new JSON().set("value", bindingValue.getFQN().toString());
               if (bindingImplementation != null)
               {
                  a.set("implementation", bindingImplementation.getFQN().toString());
               }
               if (scope != null)
               {
                  a.set("scope", scope);
               }
               list.add(a);
            }
         }
         state.put(application.getHandle(), new JSON().set("bindings", list));
      }
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      state.remove(application.getHandle());
   }

   @Override
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      return state.get(application.getHandle());
   }
}
