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

import org.juzu.Response;
import org.juzu.UndeclaredIOException;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.request.ApplicationContext;
import org.juzu.request.MimeContext;
import org.juzu.request.RequestContext;
import org.juzu.text.Printer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Template
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

   public void render() throws TemplateExecutionException, UndeclaredIOException
   {
      render(Collections.<String, Object>emptyMap(), null);
   }

   public void render(Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      render(Collections.<String, Object>emptyMap(), locale);
   }

   public void render(Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException
   {
      render(parameters, null);
   }

   public void render(Map<String, ?> parameters, Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      try
      {
         RequestContext context = InternalApplicationContext.getCurrentRequest();
         if (context instanceof MimeContext)
         {
            MimeContext mime = (MimeContext)context;
            Response.Mime stream = ok(parameters, locale);
            mime.setResponse(stream);
         }
         else
         {
            throw new AssertionError("does not make sense");
         }
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }

   }

   public void render(Printer printer) throws TemplateExecutionException, UndeclaredIOException
   {
      render(printer, Collections.<String, Object>emptyMap(), null);
   }

   public void render(Printer printer, Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      render(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public void render(Printer printer, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException
   {
      render(printer, parameters, null);
   }

   public final Response.Mime ok()
   {
      return ok(null, null);
   }

   public final Response.Mime ok(Locale locale)
   {
      return ok(null, locale);
   }

   public final Response.Mime ok(Map<String, ?> parameters)
   {
      return ok(parameters, null);
   }

   public final Response.Mime ok(Map<String, ?> parameters, Locale locale)
   {
      final TemplateRenderContext trc = applicationContext.render(this, parameters, locale);
      return new Response.Mime.Render()
      {
         
         @Override
         public String getTitle()
         {
            return trc.getTitle();
         }

         public void send(Printer printer) throws IOException
         {
            trc.render(printer);
         }
      };
   }

   public final Response.Mime.Resource notFound()
   {
      return notFound(null, null);
   }

   public final Response.Mime.Resource notFound(Locale locale)
   {
      return notFound(null, locale);
   }

   public final Response.Mime.Resource notFound(Map<String, ?> parameters)
   {
      return notFound(parameters, null);
   }

   public final Response.Mime.Resource notFound(Map<String, ?> parameters, Locale locale)
   {
      final TemplateRenderContext trc = applicationContext.render(this, parameters, locale);
      return new Response.Mime.Resource()
      {
         @Override
         public int getStatus()
         {
            return 404;
         }
         public void send(Printer printer) throws IOException
         {
            trc.render(printer);
         }
      };
   }

   public abstract Builder with();

   public Builder with(Locale locale)
   {
      Builder builder = with();
      builder.locale = locale;
      return builder;
   }

   /**
    * Renders the template.
    *
    * @param printer the printer
    * @param attributes the attributes
    * @param locale the locale
    * @throws TemplateExecutionException any execution exception
    * @throws UndeclaredIOException any io exception
    */
   public void render(
      Printer printer,
      Map<String, ?> attributes,
      Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      if (printer == null)
      {
         throw new NullPointerException("No null printe provided");
      }
      try
      {
         TemplateRenderContext trc = applicationContext.render(this, attributes, locale);
         trc.render(printer);
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
   }
   
   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[path=" + path + "]";
   }

   public class Builder
   {

      /** The parameters. */
      private Map<String, Object> parameters;

      /** The locale. */
      private Locale locale;

      public final void render()
      {
         Template.this.render(parameters, locale);
      }

      public final Response.Mime.Stream ok()
      {
         return Template.this.ok(parameters, locale);
      }

      public final Response.Mime.Stream notFound()
      {
         return Template.this.notFound(parameters, locale);
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
