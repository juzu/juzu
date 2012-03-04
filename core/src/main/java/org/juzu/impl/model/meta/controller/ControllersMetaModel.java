package org.juzu.impl.model.meta.controller;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.meta.ApplicationMetaModel;
import org.juzu.impl.model.meta.ApplicationsMetaModel;
import org.juzu.impl.model.meta.Key;
import org.juzu.impl.model.meta.MetaModel;
import org.juzu.impl.model.meta.MetaModelEvent;
import org.juzu.impl.model.meta.MetaModelObject;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersMetaModel extends MetaModelObject implements Iterable<ControllerMetaModel>
{

   /** . */
   public final static Key<ControllersMetaModel> KEY = Key.of(ControllersMetaModel.class);

   /** . */
   MetaModel model;
   
   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("values", getChildren(ControllerMetaModel.class));
      return json;
   }

   public Iterator<ControllerMetaModel> iterator()
   {
      return getChildren(ControllerMetaModel.class).iterator();
   }

   ControllerMetaModel get(ElementHandle.Class handle)
   {
      return getChild(Key.of(handle, ControllerMetaModel.class));
   }

   public ControllerMetaModel add(ElementHandle.Class handle)
   {
      Key<ControllerMetaModel> key = Key.of(handle, ControllerMetaModel.class);
      if (getChild(key) != null)
      {
         throw new IllegalStateException();
      }
      ControllerMetaModel controller = new ControllerMetaModel(model, handle);
      addChild(key, controller);
      return controller;
   }

   public void processControllerMethod(
      ExecutableElement methodElt,
      String annotationName,
      Map<String, Object> annotationValues) throws CompilationException
   {
      TypeElement controllerElt = (TypeElement)methodElt.getEnclosingElement();
      ElementHandle.Class handle = ElementHandle.Class.create(controllerElt);
      ControllerMetaModel controller = get(handle);
      if (controller == null)
      {
         controller = add(handle);
      }
      controller.addMethod(
         methodElt,
         annotationName,
         annotationValues);
   }

   public void postProcess(MetaModel model)
   {
      for (ControllerMetaModel controller : getChildren(ControllerMetaModel.class))
      {
         if (controller.controllers == null)
         {
            PackageElement packageElt = model.env.getPackageOf(model.env.get(controller.handle));
            QN packageQN = new QN(packageElt.getQualifiedName());
            for (ApplicationMetaModel application : model.getChild(ApplicationsMetaModel.KEY))
            {
               if (application.getFQN().getPackageName().isPrefix(packageQN))
               {
                  application.getControllers().add(controller);
                  controller.modified = false;
               }
            }
         }
         else
         {
            if (controller.modified)
            {
               MetaModel.queue(MetaModelEvent.createUpdated(controller));
               controller.modified = false;
            }
         }
      }
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      model = (MetaModel)parent;
   }
}
