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
import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.application.InternalApplicationContext;
import org.juzu.request.MimeContext;
import org.juzu.request.RequestContext;
import org.juzu.io.CharStream;

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

   public Response.Render render() throws TemplateExecutionException, UndeclaredIOException
   {
      return render(Collections.<String, Object>emptyMap(), null);
   }

   public Response.Render render(Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      return render(Collections.<String, Object>emptyMap(), locale);
   }

   public Response.Render render(Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException
   {
      return render(parameters, null);
   }

   public Response.Render render(Map<String, ?> parameters, Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      try
      {
         RequestContext context = InternalApplicationContext.getCurrentRequest();
         if (context instanceof MimeContext)
         {
            MimeContext mime = (MimeContext)context;
            final TemplateRenderContext renderContext = applicationContext.render(this, parameters, locale);
            Response.Render render = new Response.Content.Render()
            {
               @Override
               public String getTitle()
               {
                  return renderContext.getTitle();
               }
               public void send(CharStream stream) throws IOException
               {
                  renderContext.render(stream);
               }
            };
            mime.setResponse(render);
            return render;
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

   public final Response.Content.Resource notFound()
   {
      return notFound(null, null);
   }

   public final Response.Content.Resource notFound(Locale locale)
   {
      return notFound(null, locale);
   }

   public final Response.Content.Resource notFound(Map<String, ?> parameters)
   {
      return notFound(parameters, null);
   }

   public final Response.Content.Resource<CharStream> notFound(Map<String, ?> parameters, Locale locale)
   {
      final TemplateRenderContext trc = applicationContext.render(this, parameters, locale);
      return new Response.Content.Resource<CharStream>()
      {
         @Override
         public Class<CharStream> getKind()
         {
            return CharStream.class;
         }
         @Override
         public int getStatus()
         {
            return 404;
         }
         @Override
         public void send(CharStream stream) throws IOException
         {
            trc.render(stream);
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

   public void renderTo(CharStream printer) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, Collections.<String, Object>emptyMap(), null);
   }

   public void renderTo(CharStream printer, Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public void renderTo(CharStream printer, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, parameters, null);
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
   public void renderTo(
      CharStream printer,
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

      public final Response.Render render()
      {
         return Template.this.render(parameters, locale);
      }

      public final Response.Content notFound()
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
