/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.model.meta;

import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.meta.controller.ControllerMetaModel;
import org.juzu.impl.model.meta.controller.ControllersMetaModel;
import org.juzu.impl.model.meta.template.TemplateRefMetaModel;
import org.juzu.impl.model.meta.template.TemplateRefsMetaModel;
import org.juzu.impl.model.processor.ModelHandler;
import org.juzu.impl.model.processor.ProcessingContext;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class MetaModel extends MetaModelObject implements ModelHandler
{

   /** . */
   public static final Logger log = BaseProcessor.getLogger(MetaModel.class);

   /** . */
   public ProcessingContext env;

   /** . */
   private final LinkedList<MetaModelEvent> queue = new LinkedList<MetaModelEvent>();

   /** . */
   private static final ThreadLocal<MetaModel> current = new ThreadLocal<MetaModel>();

   public MetaModel()
   {
      addChild(ControllersMetaModel.KEY, new ControllersMetaModel());
      addChild(TemplateRefsMetaModel.KEY, new TemplateRefsMetaModel());
      addChild(ApplicationsMetaModel.KEY, new ApplicationsMetaModel());
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("applications", getChild(ApplicationsMetaModel.KEY));
      json.add("templates", getChild(TemplateRefsMetaModel.KEY));
      json.add("controllers", getChild(ControllersMetaModel.KEY));
      return json;
   }

   //

   public ApplicationMetaModel addApplication(String packageName, String applicationName)
   {
      return getChild(ApplicationsMetaModel.KEY).add(ElementHandle.Package.create(new QN(packageName)), applicationName, null, null, Collections.<FQN>emptyList());
   }

   public TemplateRefMetaModel addTemplateRef(String className, String fieldName, String path)
   {
      return getChild(TemplateRefsMetaModel.KEY).add(ElementHandle.Field.create(new FQN(className), fieldName), path);
   }

   public ControllerMetaModel addController(String className)
   {
      return getChild(ControllersMetaModel.KEY).add(ElementHandle.Class.create(new FQN(className)));
   }

   //

   public void processControllerMethod(
      ExecutableElement methodElt,
      String annotationName,
      Map<String, Object> annotationValues) throws CompilationException
   {
      getChild(ControllersMetaModel.KEY).processControllerMethod(methodElt, annotationName, annotationValues);
   }

   public void processDeclarationTemplate(
      VariableElement variableElt,
      String annotationName,
      Map<String, Object> annotationValues) throws CompilationException
   {
      getChild(TemplateRefsMetaModel.KEY).processDeclarationTemplate(variableElt, annotationName, annotationValues);
   }

   public void processApplication(
      PackageElement packageElt,
      String annotationName,
      Map<String, Object> annotationValues) throws CompilationException
   {
      getChild(ApplicationsMetaModel.KEY).processApplication(packageElt, annotationName, annotationValues);
   }

   public void postActivate(ProcessingContext env)
   {
      this.env = env;
      current.set(this);
      garbage(this, this, new HashSet<MetaModelObject>());
      postActivate(this, this, new HashSet<MetaModelObject>());
   }

   private void garbage(MetaModel model, MetaModelObject object, HashSet<MetaModelObject> visited)
   {
      if (!visited.contains(object))
      {
         visited.add(this);

         //
         for (MetaModelObject child : object.getChildren())
         {
            garbage(model, child, visited);
         }

         //
         if (!object.exist(model))
         {
            object.remove();
         }
      }
   }

   private void postActivate(MetaModel model, MetaModelObject object, HashSet<MetaModelObject> visited)
   {
      if (!visited.contains(object))
      {
         object.postActivate(model);
         visited.add(this);
         for (MetaModelObject child : object.getChildren())
         {
            postActivate(model, child, visited);
         }
      }
   }

   public void postProcess() throws CompilationException
   {
      postProcess(this, this, new HashSet<MetaModelObject>());
   }

   private void postProcess(MetaModel model, MetaModelObject object, HashSet<MetaModelObject> visited)
   {
      if (!visited.contains(object))
      {
         object.postProcess(model);
         visited.add(this);
         for (MetaModelObject child : object.getChildren())
         {
            postProcess(model, child, visited);
         }
      }
   }

   public void prePassivate()
   {
      try
      {
         prePassivate(this, this, new HashSet<MetaModelObject>());
      }
      finally
      {
         this.env = null;
         current.set(null);
      }
   }

   private void prePassivate(MetaModel model, MetaModelObject object, HashSet<MetaModelObject> visited)
   {
      if (!visited.contains(object))
      {
         object.prePassivate(model);
         visited.add(this);
         for (MetaModelObject child : object.getChildren())
         {
            prePassivate(model, child, visited);
         }
      }
   }

   public List<MetaModelEvent> popEvents()
   {
      ArrayList<MetaModelEvent> copy = new ArrayList<MetaModelEvent>(queue);
      queue.clear();
      return copy;
   }

   public MetaModelEvent popEvent()
   {
      return queue.isEmpty() ? null : queue.removeFirst();
   }

   public boolean hasEvents()
   {
      return !queue.isEmpty();
   }

   public static void queue(MetaModelEvent event)
   {
      MetaModel model = current.get();
      if (model != null)
      {
         MetaModel.log.log("Queue event " + event.getType() + " " + event.getObject());
         model.queue.add(event);
      }
   }
}
