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

package org.juzu.impl.model.meta.template;

import org.juzu.impl.model.meta.Key;
import org.juzu.impl.model.meta.MetaModel;
import org.juzu.impl.model.meta.MetaModelEvent;
import org.juzu.impl.model.meta.MetaModelObject;
import org.juzu.impl.utils.JSON;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateMetaModel extends MetaModelObject
{

   /** . */
   public final static Key<TemplateMetaModel> KEY = Key.of(TemplateMetaModel.class);

   /** The related application. */
   ApplicationTemplatesMetaModel templates;

   /** . */
   final String path;

   public TemplateMetaModel(String path)
   {
      this.path = path;
   }

   public ApplicationTemplatesMetaModel getTemplates()
   {
      return templates;
   }

   public String getPath()
   {
      return path;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("path", path);
      json.add("refs", getKeys(TemplateRefMetaModel.class));
      return json;
   }

   public Collection<TemplateRefMetaModel> getRefs()
   {
      ArrayList<TemplateRefMetaModel> refs = new ArrayList<TemplateRefMetaModel>();
      for (MetaModelObject parent : getParents())
      {
         if (parent instanceof TemplateRefMetaModel)
         {
            refs.add((TemplateRefMetaModel)parent);
         }
      }
      return refs;
   }

   /** . */
   private int refCount = 0;

   @Override
   public boolean exist(MetaModel model)
   {
      return refCount == 0 || templates == null;
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      if (parent instanceof ApplicationTemplatesMetaModel)
      {
         this.templates = (ApplicationTemplatesMetaModel)parent;
      }
      else if (parent instanceof TemplateRefMetaModel)
      {
         refCount++;
      }
   }

   @Override
   protected void preDetach(MetaModelObject parent)
   {
      if (parent instanceof ApplicationTemplatesMetaModel)
      {
         MetaModel.queue(MetaModelEvent.createRemoved(this, templates.application.getHandle()));
         this.templates = null;
      }
      else if (parent instanceof TemplateRefMetaModel)
      {
         refCount--;
      }
   }
}
