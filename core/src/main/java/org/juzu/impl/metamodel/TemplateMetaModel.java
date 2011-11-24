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

import org.juzu.impl.processor.ElementHandle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateMetaModel extends MetaModelObject
{

   /** The references pointing to this template. */
   final LinkedHashMap<ElementHandle.Field, TemplateRefMetaModel> refs = new LinkedHashMap<ElementHandle.Field, TemplateRefMetaModel>();

   /** The related application. */
   ApplicationMetaModel application;

   /** . */
   final String path;

   public TemplateMetaModel(ApplicationMetaModel application, String path)
   {
      this.application = application;
      this.path = path;
   }

   public ApplicationMetaModel getApplication()
   {
      return application;
   }

   public String getPath()
   {
      return path;
   }

   public Map<String, ?> toJSON()
   {
      TreeMap<String, Object> json = new TreeMap<String, Object>();
      json.put("path", path);
      json.put("refs", new HashSet<ElementHandle.Field>(refs.keySet()));
      json.put("application", application.fqn);
      return json;
   }

   public Collection<TemplateRefMetaModel> getRefs()
   {
      return new ArrayList<TemplateRefMetaModel>(refs.values());
   }

   public TemplateMetaModel addRef(TemplateRefMetaModel ref)
   {
      if (ref.template != null)
      {
         throw new IllegalArgumentException();
      }
      if (refs.containsKey(ref.handle))
      {
         throw new IllegalStateException();
      }
      if (!path.equals(ref.path))
      {
         throw new IllegalArgumentException(path + " != " + ref.path);
      }
      refs.put(ref.handle, ref);
      ref.template = this;
      application.model.queue.add(new MetaModelEvent(MetaModelEvent.AFTER_ADD, ref));
      return this;
   }

   public void removeRef(TemplateRefMetaModel ref)
   {
      if (ref.template != this)
      {
         throw new IllegalArgumentException();
      }
      if (refs.containsKey(ref.handle))
      {
         application.model.queue.add(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, ref));
         refs.remove(ref.handle);
         ref.template = null;
      }
      else
      {
         throw new IllegalStateException();
      }
   }
}
