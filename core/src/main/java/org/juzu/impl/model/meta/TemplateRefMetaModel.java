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

/**
 * A reference to a template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateRefMetaModel extends MetaModelObject
{

   /** The related template if it exist. */
   TemplateMetaModel template;

   /** . */
   final ElementHandle.Field handle;

   /** . */
   String path;

   TemplateRefMetaModel(
      ElementHandle.Field handle,
      String path)
   {
      this.handle = handle;
      this.path = path;
   }

   public ElementHandle.Field getHandle()
   {
      return handle;
   }

   public String getPath()
   {
      return path;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("handle", handle);
      json.add("template", template == null ? null : template.toJSON());
      return json;
   }
}
