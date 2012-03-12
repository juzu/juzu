package org.juzu.impl.controller.metamodel;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.Key;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.utils.JSON;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersMetaModel extends MetaModelObject implements Iterable<ControllerMetaModel>
{

   /** . */
   public final static Key<ControllersMetaModel> KEY = Key.of(ControllersMetaModel.class);

   /** . */
   String defaultController;

   /** . */
   Boolean escapeXML;

   public ControllersMetaModel()
   {
   }

   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.map("values", getChildren(ControllerMetaModel.class));
      return json;
   }

   public Iterator<ControllerMetaModel> iterator()
   {
      return getChildren(ControllerMetaModel.class).iterator();
   }
   
   public ControllerMetaModel get(ElementHandle.Class handle)
   {
      return getChild(Key.of(handle, ControllerMetaModel.class));
   }

   public void add(ControllerMetaModel controller)
   {
      addChild(Key.of(controller.handle, ControllerMetaModel.class), controller);
   }

   public void remove(ControllerMetaModel controller)
   {
      if (controller.controllers != this)
      {
         throw new IllegalArgumentException();
      }
      removeChild(Key.of(controller.handle, ControllerMetaModel.class));
   }

   public MethodMetaModel resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException
   {
      TreeSet<MethodMetaModel> set = new TreeSet<MethodMetaModel>(
         new Comparator<MethodMetaModel>()
         {
            public int compare(MethodMetaModel o1, MethodMetaModel o2)
            {
               return ((Integer)o1.parameterNames.size()).compareTo(o2.parameterNames.size());
            }
         }
      );
      for (ControllerMetaModel controller : getChildren(ControllerMetaModel.class))
      {
         for (MethodMetaModel method : controller.getMethods())
         {
            boolean add = false;
            if (typeName == null || controller.getHandle().getFQN().getSimpleName().equals(typeName))
            {
               if (method.name.equals(methodName) && method.parameterNames.containsAll(parameterNames))
               {
                  add = true;
               }
            }
            MetaModel.log.log("Method " + method + ( add ? " added to" : " removed from" ) +  " search");
            if (add)
            {
               set.add(method);
            }
         }
      }
      if (set.size() >= 1)
      {
         MethodMetaModel method = set.iterator().next();
         MetaModel.log.log("Resolved method " + method.getName() + " " + method.getParameterNames() + " for " + methodName + " "
            + parameterNames + " among " + set);
         return method;
      }
      else
      {
         MetaModel.log.log("Could not resolve method " + methodName + " " + parameterNames + " among " + set);
         return null;
      }
   }
}
