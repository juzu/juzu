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
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.QN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModelObject
{

   /** . */
   private static final Logger log = BaseProcessor.getLogger(ApplicationMetaModel.class);

   /** The controllers. */
   LinkedHashMap<ElementHandle.Class, ControllerMetaModel> controllers = new LinkedHashMap<ElementHandle.Class, ControllerMetaModel>();

   /** The templates. */
   LinkedHashMap<String, TemplateMetaModel> templates = new LinkedHashMap<String, TemplateMetaModel>();

   /** . */
   final ElementHandle.Package handle;

   /** . */
   final FQN fqn;

   /** . */
   final String defaultController;

   /** . */
   final Boolean escapeXML;

   /** . */
   final QN templatesQN;

   /** . */
   final MetaModel model;

   /** . */
   final List<FQN> plugins;

   /** . */
   boolean modified;

   ApplicationMetaModel(
      MetaModel model,
      ElementHandle.Package handle,
      String applicationName,
      String defaultController,
      Boolean escapeXML,
      List<FQN> plugins)
   {
      this.model = model;
      this.handle = handle;
      this.fqn = new FQN(handle.getQN(), applicationName);
      this.defaultController = defaultController;
      this.escapeXML = escapeXML;
      this.templatesQN = fqn.getPackageName().append("templates");
      this.modified = false;
      this.plugins = plugins;
   }

   public String getDefaultController()
   {
      return defaultController;
   }

   public Boolean getEscapeXML()
   {
      return escapeXML;
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
      return new ArrayList<ControllerMetaModel>(controllers.values());
   }

   public TemplateMetaModel addTemplate(TemplateRefMetaModel ref)
   {
      if (templates.containsKey(ref.path))
      {
         throw new IllegalStateException("Template path already existing");
      }
      TemplateMetaModel template = new TemplateMetaModel(this, ref);
      templates.put(template.path, template);
      model.queue(new MetaModelEvent(MetaModelEvent.AFTER_ADD, template));
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
      model.queue(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, template));
      for (TemplateRefMetaModel ref : template.getRefs())
      {
         template.removeRef(ref);
      }
      templates.remove(template.getPath());
      template.application = null;
   }

   public void addController(ControllerMetaModel controller)
   {
      if (controllers.containsKey(controller.handle))
      {
         throw new IllegalStateException();
      }
      controllers.put(controller.handle, controller);
      controller.application = this;
      model.queue(new MetaModelEvent(MetaModelEvent.AFTER_ADD, controller));
   }

   public void removeController(ControllerMetaModel controller)
   {
      if (controller.application != this)
      {
         throw new IllegalArgumentException();
      }
      if (controllers.containsKey(controller.handle))
      {
         model.queue(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, controller));
         controllers.remove(controller.handle);
         controller.application = null;
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   public List<FQN> getPlugins()
   {
      return plugins;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("handle", handle);
      json.add("fqn", fqn);
      json.add("defaultController", defaultController);
      json.add("templates", templates.values());
      json.add("controllers", controllers.keySet());
      json.add("plugins", plugins);
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
      log.log("About to search method in controllers " + controllers.keySet());
      for (ControllerMetaModel controller : controllers.values())
      {
         for (MethodMetaModel method : controller.methods.values())
         {
            boolean add = false;
            if (typeName == null || controller.getHandle().getFQN().getSimpleName().equals(typeName))
            {
               if (method.name.equals(methodName) && method.parameterNames.containsAll(parameterNames))
               {
                  add = true;
               }
            }
            log.log("Method " + method + ( add ? " added to" : " removed from" ) +  " search");
            if (add)
            {
               set.add(method);
            }
         }
      }
      if (set.size() >= 1)
      {
         MethodMetaModel method = set.iterator().next();
         log.log("Resolved method " + method.getName() + " " + method.getParameterNames() + " for " + methodName + " "
            + parameterNames + " among " + set);
         return method;
      }
      else
      {
         log.log("Could not resolve method " + methodName + " " + parameterNames + " among " + set);
         return null;
      }
   }
}
