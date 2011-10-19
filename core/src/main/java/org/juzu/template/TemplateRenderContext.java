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

package org.juzu.template;

import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.text.Printer;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderContext
{

   /** . */
   private final Printer printer;

   /** . */
   private final Map<String, ?> attributes;

   /** . */
   private final Locale locale;

   /** . */
   private String title;

   public TemplateRenderContext(Printer printer)
   {
      this(printer, Collections.<String, Object>emptyMap());
   }

   public TemplateRenderContext(Printer printer, Map<String, ?> attributes)
   {
      this(printer, attributes, null);
   }

   public TemplateRenderContext(Printer printer, Locale locale)
   {
      this(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public TemplateRenderContext(Printer printer, Map<String, ?> attributes, Locale locale)
   {
      this.printer = printer;
      this.locale = locale;
      this.attributes = attributes;
   }

   public Map<String, ?> getAttributes()
   {
      return attributes;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public TemplateStub resolveTemplate(String path)
   {
      return null;
   }

   public Object resolveBean(String expression)
   {
      return null;
   }
}
