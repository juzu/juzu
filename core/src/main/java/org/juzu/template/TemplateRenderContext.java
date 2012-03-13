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

import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.io.CharStream;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderContext
{

   /** . */
   private final Map<String, ?> attributes;

   /** . */
   private final Locale locale;

   /** . */
   private String title;

   /** . */
   protected CharStream printer;

   /** . */
   private final TemplateStub stub;

   public TemplateRenderContext(TemplateStub stub)
   {
      this(stub, Collections.<String, Object>emptyMap());
   }

   public TemplateRenderContext(TemplateStub stub, Map<String, ?> attributes)
   {
      this(stub, attributes, null);
   }

   public TemplateRenderContext(TemplateStub stub, Locale locale)
   {
      this(stub, Collections.<String, Object>emptyMap(), locale);
   }

   public TemplateRenderContext(TemplateStub stub, Map<String, ?> attributes, Locale locale)
   {
      this.locale = locale;
      this.attributes = attributes;
      this.stub = stub;
   }

   public Map<String, ?> getAttributes()
   {
      return attributes;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public CharStream getPrinter()
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

   public Object resolveBean(String expression) throws ApplicationException
   {
      return null;
   }

   public void render(CharStream printer) throws IOException
   {
      if (this.printer != null)
      {
         throw new IllegalStateException("Already rendering");
      }

      //
      this.printer = printer;

      //
      try
      {
         stub.render(this);
      }
      finally
      {
         this.printer = null;
      }
   }
}
