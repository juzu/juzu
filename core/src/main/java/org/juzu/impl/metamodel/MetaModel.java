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
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class MetaModel extends MetaModelObject
{

   /** . */
   public static final Logger log = BaseProcessor.getLogger(MetaModel.class);

   /** . */
   public ProcessingContext env;

   /** . */
   private final EventQueue queue = new EventQueue();

   /** . */
   private final EventQueue dispatch = new EventQueue();

   /** . */
   private static final ThreadLocal<MetaModel> current = new ThreadLocal<MetaModel>();

   /** . */
   private final ApplicationsMetaModel applications = new ApplicationsMetaModel();

   /** . */
   private boolean queuing = false;

   public MetaModel()
   {
      addChild(ApplicationsMetaModel.KEY, applications);
   }
   
   public void addPlugin(String name, MetaModelPlugin plugin)
   {
      applications.addPlugin(name, plugin);
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.map("applications", getChild(ApplicationsMetaModel.KEY));
      return json;
   }

   //

   public void postActivate(ProcessingContext env)
   {
      this.env = env;
      current.set(this);

      //
      queuing = true;
      try
      {
         //
         garbage(this, this, new HashSet<MetaModelObject>());

         //
         applications.postActivate(this);
      }
      finally
      {
         queuing = false;
      }
   }

   public void processAnnotation(Element element, String annotationFQN, Map<String, Object> annotationValues) throws CompilationException
   {
      queuing = true;
      try
      {
         MetaModel.log.log("Processing annotation " + element);
         applications.processAnnotation(this, element, annotationFQN, annotationValues);
      }
      finally
      {
         queuing = false;
      }
   }

   public void postProcess() throws CompilationException
   {
      queuing = true;
      try
      {
         applications.postProcessAnnotations(this);
      }
      finally
      {
         queuing = false;
      }

      // For now we do this way lter we poll
      applications.processEvents(this, dispatch);

      //
      MetaModel.log.log("Post processing");
      applications.postProcessEvents(this);
   }

   public void prePassivate()
   {
      try
      {
         applications.prePassivate(this);
      }
      finally
      {
         this.env = null;
         current.set(null);
      }
   }

   //

   public ApplicationMetaModel addApplication(String packageName, String applicationName)
   {
      return applications.add(ElementHandle.Package.create(QN.parse(packageName)), applicationName);
   }

   //

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

   @Override
   public void queue(MetaModelEvent event)
   {
      if (!queuing)
      {
         throw new IllegalStateException("Not queueing");
      }
      queue.queue(event);
      dispatch.queue(event);
   }

   public EventQueue getQueue()
   {
      return queue;
   }
}
