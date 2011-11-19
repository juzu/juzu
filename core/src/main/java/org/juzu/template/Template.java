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

import org.juzu.request.ApplicationContext;
import org.juzu.text.Printer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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

   public String getPath()
   {
      return path;
   }

   public void render() throws TemplateExecutionException, IOException
   {
      render(Collections.<String, Object>emptyMap(), null);
   }

   public void render(Locale locale) throws TemplateExecutionException, IOException
   {
      render(Collections.<String, Object>emptyMap(), locale);
   }

   public void render(Map<String, ?> parameters) throws TemplateExecutionException, IOException
   {
      render(parameters, null);
   }

   public void render(Map<String, ?> parameters, Locale locale) throws TemplateExecutionException, IOException
   {
      render(null, parameters, null);
   }

   public void render(Printer printer) throws TemplateExecutionException, IOException
   {
      render(printer, Collections.<String, Object>emptyMap(), null);
   }

   public void render(Printer printer, Locale locale) throws TemplateExecutionException, IOException
   {
      render(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public void render(Printer printer, Map<String, ?> parameters) throws TemplateExecutionException, IOException
   {
      render(printer, parameters, null);
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
      applicationContext.render(this, printer, attributes, locale);
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[path=" + path + "]";
   }

   public abstract class Builder
   {

      /** The parameters. */
      private Map<String, Object> parameters;

      public final void render() throws IOException
      {
         if (parameters != null)
         {
            Template.this.render(parameters);
         }
         else
         {
            Template.this.render();
         }
      }

      /**
       * Update a parameter, if the value is not null the parameter with the specified name is set, otherwise
       * the parameter is removed. If the parameter is set and a value was set previously, the old value is
       * overwritten otherwise. If the parameter is removed and the value does not exist, nothing happens.
       *
       * @param name the parameter name
       * @param value the parameter value
       * @return this builder
       * @throws NullPointerException if the name argument is null
       */
      public Builder set(String name, Object value) throws NullPointerException
      {
         if (name == null)
         {
            throw new NullPointerException("The parameter argument cannot be null");
         }
         if (value != null)
         {
            if (parameters == null)
            {
               parameters = new HashMap<String, Object>();
            }
            parameters.put(name, value);
         }
         else if (parameters != null)
         {
            parameters.remove(name);
         }
         return this;
      }
   }
}
