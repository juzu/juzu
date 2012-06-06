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

package juzu.template;

import juzu.PropertyMap;
import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.application.ApplicationContext;
import juzu.impl.request.Request;
import juzu.impl.utils.Path;
import juzu.io.AppendableStream;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.request.MimeContext;
import juzu.request.RequestContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Template
{

   /** . */
   private final Path path;

   /** . */
   private final ApplicationContext applicationContext;

   public Template(ApplicationContext applicationContext, String path)
   {
      this(applicationContext, Path.parse(path));
   }

   public Template(ApplicationContext applicationContext, Path path)
   {
      this.applicationContext = applicationContext;
      this.path = path;
   }

   public Path getPath()
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

   public Response.Render render(final Map<String, ?> parameters, final Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      try
      {
         RequestContext context = Request.getCurrent().getContext();
         if (context instanceof MimeContext)
         {
            MimeContext mime = (MimeContext)context;
            PropertyMap properties = new PropertyMap();
            TemplateRenderContext streamable = applicationContext.render(Template.this, properties, parameters, locale);
            StringBuilder sb = new StringBuilder();
            streamable.render(new AppendableStream(sb));
            Response.Render render = new Response.Content.Render(properties, new Streamable.CharSequence(sb));
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

   public final Response.Content.Resource<Stream.Char> notFound(Map<String, ?> parameters, Locale locale)
   {
      StringBuilder sb = new StringBuilder();
      renderTo(new AppendableStream(sb), parameters, locale);
      return Response.status(404, sb.toString());
   }

   public abstract Builder with();

   public Builder with(Locale locale)
   {
      Builder builder = with();
      builder.locale = locale;
      return builder;
   }

   public void renderTo(Stream.Char printer) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, Collections.<String, Object>emptyMap(), null);
   }

   public void renderTo(Stream.Char printer, Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, Collections.<String, Object>emptyMap(), locale);
   }

   public void renderTo(Stream.Char printer, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException
   {
      renderTo(printer, parameters, null);
   }

   /**
    * Renders the template.
    *
    * @param printer the printer
    * @param parameters the attributes
    * @param locale the locale
    * @throws TemplateExecutionException any execution exception
    * @throws UndeclaredIOException any io exception
    */
   public void renderTo(
      Stream.Char printer,
      Map<String, ?> parameters,
      Locale locale) throws TemplateExecutionException, UndeclaredIOException
   {
      if (printer == null)
      {
         throw new NullPointerException("No null printe provided");
      }
      try
      {
         TemplateRenderContext trc = applicationContext.render(this, null, parameters, locale);
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
