package org.juzu.impl.controller.metamodel;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.Key;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;

import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersMetaModel extends MetaModelObject implements Iterable<ControllerMetaModel>
{

   /** . */
   public final static Key<ControllersMetaModel> KEY = Key.of(ControllersMetaModel.class);

   /** . */
   FQN defaultController;

   /** . */
   Boolean escapeXML;

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

   public ControllerMethodMetaModel resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException
   {
      try
      {
         ControllerMethodMetaModelResolver resolver = new ControllerMethodMetaModelResolver(this);
         return resolver.resolve(typeName, methodName, parameterNames);
      }
      catch (AmbiguousResolutionException e)
      {
         MetaModel.log.log("Could not resolve ambiguous method " + methodName + " " + parameterNames);
         return null;
      }
   }
}
