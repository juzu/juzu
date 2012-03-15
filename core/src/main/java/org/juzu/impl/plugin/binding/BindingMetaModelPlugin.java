package org.juzu.impl.plugin.binding;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.binding.Bindings;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
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
         Object value = values.get("value");
         ArrayList<JSON> list = new ArrayList<JSON>();
         if (value != null)
         {
            List<?> bindings;
            if (value instanceof List<?>)
            {
               bindings = new ArrayList<Object>((List<?>)value);
            }
            else 
            {
               bindings = Collections.singletonList(value);
            }
            for (int i = 0; i < bindings.size(); i++)
            {
               AnnotationMirror binding;
               Object v = bindings.get(i);
               if(v instanceof AnnotationValue) 
               {
                  binding = (AnnotationMirror)((AnnotationValue) v).getValue();
               }
               else
               {
                  binding = (AnnotationMirror) v;
               }
               
               Map<String, Object> bindingValues = Tools.foo(binding);
               TypeMirror bindingValue = (TypeMirror)bindingValues.get("value");
               TypeMirror bindingImplementation = (TypeMirror)bindingValues.get("implementation");
               VariableElement scope = (VariableElement)bindingValues.get("scope");
               JSON a = new JSON().set("value", bindingValue.toString());
               if (bindingImplementation != null)
               {
                  a.set("implementation", bindingImplementation.toString());
               }
               if (scope != null)
               {
                  a.set("scope", scope.getSimpleName().toString().toLowerCase());
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
