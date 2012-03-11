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

package org.juzu.impl.application.metamodel;

import org.juzu.Application;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.controller.metamodel.ApplicationControllersMetaModel;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.template.metamodel.ApplicationTemplatesMetaModel;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModel extends MetaModelObject
{

   /** . */
   final ElementHandle.Package handle;

   /** . */
   final FQN fqn;

   /** . */
   final Boolean escapeXML;

   /** . */
   public MetaModel model;

   /** . */
   final List<FQN> plugins;

   /** . */
   boolean modified;

   ApplicationMetaModel(
      ElementHandle.Package handle,
      String applicationName,
      String defaultController,
      Boolean escapeXML,
      List<FQN> plugins)
   {
      FQN fqn = new FQN(handle.getQN(), applicationName);

      //
      addChild(ApplicationControllersMetaModel.KEY, new ApplicationControllersMetaModel(defaultController));
      addChild(ApplicationTemplatesMetaModel.KEY, new ApplicationTemplatesMetaModel(fqn.getPackageName().append("templates")));

      //
      this.handle = handle;
      this.fqn = fqn;
      this.escapeXML = escapeXML;
      this.modified = false;
      this.plugins = plugins;
   }

   public ApplicationControllersMetaModel getControllers()
   {
      return getChild(ApplicationControllersMetaModel.KEY);
   }

   public ApplicationTemplatesMetaModel getTemplates()
   {
      return getChild(ApplicationTemplatesMetaModel.KEY);
   }

   public String getDefaultController()
   {
      return getControllers().getDefaultController();
   }

   public Boolean getEscapeXML()
   {
      return escapeXML;
   }

   public FQN getFQN()
   {
      return fqn;
   }

   public ElementHandle.Package getHandle()
   {
      return handle;
   }

   public List<FQN> getPlugins()
   {
      return plugins;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.set("handle", handle);
      json.set("fqn", fqn);
      json.setList("templates", getTemplates());
      json.setList("controllers", getControllers());
      json.setList("plugins", plugins);
      return json;
   }

   @Override
   public boolean exist(MetaModel model)
   {
      PackageElement element = model.env.get(handle);
      boolean found = false;
      if (element != null)
      {
         for (AnnotationMirror annotationMirror : element.getAnnotationMirrors())
         {
            if (found = ((TypeElement)annotationMirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(Application.class.getName()))
            {
               break;
            }
         }
      }
      return found;
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      if (parent instanceof ApplicationsMetaModel)
      {
         model = ((ApplicationsMetaModel)parent).model;
      }
   }

   @Override
   protected void preDetach(MetaModelObject parent)
   {
      if (parent instanceof ApplicationsMetaModel)
      {
         MetaModel.queue(MetaModelEvent.createRemoved(this));
         model = null;
      }
   }
}
