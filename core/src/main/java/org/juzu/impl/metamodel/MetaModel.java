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

package org.juzu.impl.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.controller.metamodel.ControllerMetaModel;
import org.juzu.impl.controller.metamodel.ControllersMetaModel;
import org.juzu.impl.template.metamodel.TemplateRefMetaModel;
import org.juzu.impl.template.metamodel.TemplateRefsMetaModel;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class MetaModel extends MetaModelObject
{

   /** . */
   public static final Logger log = BaseProcessor.getLogger(MetaModel.class);

   /** . */
   public ProcessingContext env;

   /** . */
   private final LinkedList<MetaModelEvent> queue = new LinkedList<MetaModelEvent>();

   /** . */
   private final LinkedList<MetaModelEvent> queue2 = new LinkedList<MetaModelEvent>();

   /** . */
   private static final ThreadLocal<MetaModel> current = new ThreadLocal<MetaModel>();

   /** The meta model plugins. */
   private LinkedHashMap<String, MetaModelPlugin> plugins;

   public MetaModel()
   {
      this.plugins = new LinkedHashMap<String, MetaModelPlugin>();

      //
      addPlugin("application", new ApplicationMetaModelPlugin());
   }
   
   public void addPlugin(String name, MetaModelPlugin plugin)
   {
      plugins.put(name, plugin);
      
      //
      plugin.init(this);
   }

   public LinkedHashMap<String, MetaModelPlugin> getPlugins()
   {
      return plugins;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.setList("applications", getChild(ApplicationsMetaModel.KEY));
      json.set("templates", getChild(TemplateRefsMetaModel.KEY));
      json.setList("controllers", getChild(ControllersMetaModel.KEY));
      return json;
   }

   //

   public void processAnnotation(Element element, String annotationFQN, Map<String, Object> annotationValues) throws CompilationException
   {
      for (MetaModelPlugin plugin : plugins.values())
      {
         plugin.processAnnotation(this, element, annotationFQN, annotationValues);
      }
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

   public void postActivate(ProcessingContext env)
   {
      this.env = env;
      current.set(this);

      //
      garbage(this, this, new HashSet<MetaModelObject>());

      //
      postActivate(this, this, new HashSet<MetaModelObject>());
      
      //
      for (MetaModelPlugin plugin : plugins.values())
      {
         plugin.postActivate(this);
      }
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

      // For now we do this way lter we poll
      for (Iterator<MetaModelEvent> i = queue2.iterator();i.hasNext();)
      {
         MetaModelEvent event = i.next();
         i.remove();
         log.log("Processing meta model event " + event.getType() + " " + event.getObject());
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.processEvent(this, event);
         }
      }

      // log.log("Processing templates");
      for (MetaModelPlugin plugin : plugins.values())
      {
         plugin.postProcess(this);
      }
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

         //
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.prePassivate(this);
         }
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
         model.queue2.add(event);
      }
   }
}
