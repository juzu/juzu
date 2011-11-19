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

package org.juzu.impl.template;

import org.juzu.impl.tags.ParamTag;
import org.juzu.impl.tags.TitleTag;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.tags.DecorateTag;
import org.juzu.impl.tags.IncludeTag;
import org.juzu.impl.tags.InsertTag;
import org.juzu.template.TagHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateCompilationContext
{

   /** . */
   private final Map<String, TagHandler> tags = new HashMap<String, TagHandler>();

   public TemplateCompilationContext()
   {
      // Built in tags

      tags.put("include", new IncludeTag());
      tags.put("insert", new InsertTag());
      tags.put("decorate", new DecorateTag());
      tags.put("title", new TitleTag());
      tags.put("param", new ParamTag());
   }

   public TagHandler resolve(String name)
   {
      return tags.get(name);
   }

   public void resolveTemplate(String path) throws IOException
   {
   }

   public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
   {
      return null;
   }

}
