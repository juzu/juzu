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

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.processor.ElementHandle;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.QN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModelObject
{

   /** The controllers. */
   LinkedHashSet<ControllerMetaModel> controllers = new LinkedHashSet<ControllerMetaModel>();

   /** The templates. */
   LinkedHashMap<String, TemplateMetaModel> templates = new LinkedHashMap<String, TemplateMetaModel>();

   /** . */
   final ElementHandle.Package handle;

   /** . */
   final FQN fqn;

   /** . */
   final String defaultController;

   /** . */
   final QN templatesQN;

   /** . */
   final MetaModel model;

   ApplicationMetaModel(
      MetaModel model,
      ElementHandle.Package handle,
      String applicationName,
      String defaultController)
   {
      this.model = model;
      this.handle = handle;
      this.fqn = new FQN(handle.getQN(), applicationName);
      this.defaultController = defaultController;
      this.templatesQN = fqn.getPackageName().append("templates");
   }

   public String getDefaultController()
   {
      return defaultController;
   }

   public QN getTemplatesQN()
   {
      return templatesQN;
   }

   public FQN getFQN()
   {
      return fqn;
   }

   public ElementHandle.Package getHandle()
   {
      return handle;
   }

   public TemplateMetaModel getTemplate(String path)
   {
      return templates.get(path);
   }

   public Collection<TemplateMetaModel> getTemplates()
   {
      return new ArrayList<TemplateMetaModel>(templates.values());
   }

   public Collection<ControllerMetaModel> getControllers()
   {
      return new ArrayList<ControllerMetaModel>(controllers);
   }

   public TemplateMetaModel addTemplate(TemplateRefMetaModel ref)
   {
      if (templates.containsKey(ref.path))
      {
         throw new IllegalStateException("Template path already existing");
      }
      TemplateMetaModel template = new TemplateMetaModel(this, ref);
      templates.put(template.path, template);
      model.queue.add(new MetaModelEvent(MetaModelEvent.AFTER_ADD, template));
      return template;
   }

   public void removeTemplate(TemplateMetaModel template)
   {
      if (template.application != this)
      {
         throw new IllegalArgumentException();
      }
      if (!templates.containsKey(template.path))
      {
         throw new IllegalStateException();
      }
      for (TemplateRefMetaModel ref : template.getRefs())
      {
         template.removeRef(ref);
      }
      model.queue.add(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, template));
      templates.remove(template.getPath());
      template.application = null;
   }

   public void addController(ControllerMetaModel controller)
   {
      if (controllers.contains(controller))
      {
         throw new IllegalStateException();
      }
      controllers.add(controller);
      controller.application = this;
      model.queue.add(new MetaModelEvent(MetaModelEvent.AFTER_ADD, controller));
   }

   public void removeController(ControllerMetaModel controller)
   {
      if (controller.application != this)
      {
         throw new IllegalArgumentException();
      }
      if (controllers.contains(controller))
      {
         model.queue.add(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, controller));
         controllers.remove(controller);
         controller.application = null;
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   public Map<String, ?> toJSON()
   {
      TreeMap<String, Object> json = new TreeMap<String, Object>();
      json.put("handle", handle);
      json.put("fqn", fqn);
      json.put("defaultController", defaultController);
      ArrayList<Map<String, ?>> foo = new ArrayList<Map<String, ?>>();
      for (TemplateMetaModel bar : templates.values())
      {
         foo.add(bar.toJSON());
      }
      json.put("templates", foo);
      ArrayList<ElementHandle.Class> juu = new ArrayList<ElementHandle.Class>();
      for (ControllerMetaModel controller : controllers)
      {
         juu.add(controller.handle);
      }
      json.put("controllers", juu);
      return json;
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
      for (ControllerMetaModel controller : controllers)
      {
         for (MethodMetaModel method : controller.methods.values())
         {
            if (typeName == null || controller.getHandle().getFQN().getSimpleName().equals(typeName))
            {
               if (method.name.equals(methodName) && method.parameterNames.containsAll(parameterNames))
               {
                  set.add(method);
               }
            }
         }
      }
      if (set.isEmpty())
      {
         return null;
      }
      else if (set.size() == 1)
      {
         return set.iterator().next();
      }
      else
      {
         return null;
      }
   }
}
