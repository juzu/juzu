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

import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.application.ApplicationTemplateRenderContext;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.text.Printer;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Template
{

   /** . */
   private final String path;

   /** . */
   private final ApplicationContext applicationContext;

   public Template(ApplicationContext applicationContext, String path)
   {
      this.applicationContext = applicationContext;
      this.path = path;
   }

   public void render(Printer printer) throws TemplateExecutionException, IOException
   {
      render(printer, Collections.<String, Object>emptyMap(), null);
   }

   public void render(Printer printer,Locale locale) throws TemplateExecutionException, IOException
   {
      render(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public void render(Printer printer,Map<String, ?> context) throws TemplateExecutionException, IOException
   {
      render(printer, context, null);
   }

   /**
    * Renders the template.
    *
    * @param printer the printer
    * @param attributes the attributes
    * @param locale the locale
    * @throws org.juzu.template.TemplateExecutionException any execution exception
    * @throws java.io.IOException any io exception
    */
   public void render(
      Printer printer,
      Map<String, ?> attributes,
      Locale locale
   ) throws TemplateExecutionException, IOException {

      if (printer == null)
      {
         // ???
         throw new NullPointerException("No printer");
      }

      //
      TemplateStub stub = applicationContext.resolveTemplateStub(path);

      //
      stub.render(new ApplicationTemplateRenderContext(applicationContext, printer, attributes, locale));
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[path=" + path + "]";
   }
}
