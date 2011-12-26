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

import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.JSON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * A template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateMetaModel extends MetaModelObject
{

   /** The references pointing to this template. */
   final LinkedHashMap<ElementHandle.Field, TemplateRefMetaModel> refs;

   /** The related application. */
   ApplicationMetaModel application;

   /** . */
   final String path;

   public TemplateMetaModel(ApplicationMetaModel application, TemplateRefMetaModel ref)
   {
      this.application = application;
      this.path = ref.path;
      this.refs = new LinkedHashMap<ElementHandle.Field, TemplateRefMetaModel>();
   }

   public ApplicationMetaModel getApplication()
   {
      return application;
   }

   public String getPath()
   {
      return path;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("path", path);
      json.add("refs", new HashSet<ElementHandle.Field>(refs.keySet()));
      json.add("application", application.fqn);
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
      application.model.queue(new MetaModelEvent(MetaModelEvent.AFTER_ADD, ref));
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
         application.model.queue(new MetaModelEvent(MetaModelEvent.BEFORE_REMOVE, ref));
         refs.remove(ref.handle);
         ref.template = null;
      }
      else
      {
         throw new IllegalStateException();
      }
   }
}
