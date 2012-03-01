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

package org.juzu;

import org.juzu.text.Printer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * <p>A response object signalling to the portal the action to take after an interaction. This object is usually returned
 * after the invocation of a controller method and instructs the aggregator the action to take.</p>
 *
 * <h2>Action response</h2>
 *
 * <h3>Redirection response</h3>
 *
 * <p>A <code>Response.Process.Action.Redirect</code> response instructs the aggregator to make a redirection to a valid URL after the
 * interaction, this kind of response is created using the factory method {@link Response#redirect(String)}:
 * <code><pre>
 *    return Response.redirect("http://www.exoplatform.org");
 * </pre></code>
 * </p>
 *
 * <h3>Proceed to render phase</h3>
 *
 * <p>A <code>Response.Update</code> response instructs the aggreator to proceed to the render phase of a valid
 * view controller, this kind of response can be created using an {@link org.juzu.request.ActionContext}, however
 * the the preferred way is to use a controller companion class that carries method factories for creating render
 * responses.</p>
 *
 * <p>Type safe {@link org.juzu.Response.Update} factory method are generated for each view or resource controller methods.
 * The signature of an render factory is obtained by using the same signature of the controller method.</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Action
 *       public {@link org.juzu.Response.Update} myAction() {
 *          return MyController_.myRender("hello");
 *       }
 *
 *       &#064;View
 *       public void myRender(String param) {
 *       }
 *    }
 * </pre></code>
 *
 * <h2>Mime response</h2>
 * 
 * <p>Mime response are used by the {@link org.juzu.request.Phase#RENDER} and the {@link org.juzu.request.Phase#RESOURCE} phases.
 * Both contains a content to be streamed to the client but still they have some noticeable differences.</p>
 * 
 * <p>The {@link org.juzu.Response.Content} class is the base response class which will work well for the two phases. However the
 * {@link org.juzu.request.Phase#RENDER} can specify an optional title and the {@link org.juzu.request.Phase#RESOURCE} can
 * specify an optional status code for the user agent response.</p>
 * 
 * <p>Responses are created using the {@link Response} factory methods such as</p>
 * 
 * <ul>
 *    <li>{@link Response#content} creates an ok response</li>
 *    <li>{@link Response#notFound} creates a not found response</li>
 * </ul>
 *
 * <p>Response can also created from {@link org.juzu.template.Template} directly:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") {@link org.juzu.template.Template} index;
 *
 *       &#064;View
 *       public {@link org.juzu.Response.Render} myView() {
 *          return index.render();
 *       }
 *
 *       &#064;Inject &#064;Path("error.gtmpl")  {@link org.juzu.template.Template} error;
 *
 *       &#064;Resource
 *       public {@link org.juzu.Response.Resource} myView() {
 *          return error.notFound();
 *       }
 *    }
 * </pre></code>
 * 
 * <p>The {@link org.juzu.template.Template.Builder} can also create responses:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") index index;
 *
 *       &#064;View
 *       public {@link org.juzu.Response.Content} myView() {
 *          return index.with().label("hello").render();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Response
{

   /**
    * A response instructing to execute a render phase of a controller method after the current interaction.
    */
   public static class Update extends Response
   {

      /** . */
      final Map<String, String> parameters;

      public Update(Map<String, String> parameters)
      {
         this.parameters = parameters;
      }

      /**
       * Set a parameter for the controller method.
       *
       * @param parameterName the parameter name
       * @param parameterValue the parameter value
       * @return this object
       */
      public Update setParameter(String parameterName, String parameterValue)
      {
         if (parameterName == null)
         {
            throw new NullPointerException();
         }
         if (parameterValue == null)
         {
            throw new NullPointerException();
         }
         if (parameterName.startsWith("juzu."))
         {
            throw new IllegalArgumentException("Parameter name cannot start with <juzu.> prefix");
         }
         this.parameters.put(parameterName, parameterValue);
         return this;
      }

      public Map<String, String> getParameters()
      {
         return parameters;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Update)
         {
            Update that = (Update)obj;
            return parameters.equals(that.parameters);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "Response.Update[parameters" + parameters + "]";
      }
   }

   /**
    * A response instructing to execute an HTTP redirection after the current interaction.
    */
   public static class Redirect extends Response
   {

      /** . */
      private final String location;

      public Redirect(String location)
      {
         this.location = location;
      }

      public String getLocation()
      {
         return location;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Redirect)
         {
            Redirect that = (Redirect)obj;
            return location.equals(that.location);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "Response.Redirect[location" + location + "]";
      }
   }

   public static abstract class Content extends Response
   {

      public abstract void send(Printer printer) throws IOException;

   }

   public static abstract class Render extends Content
   {

      /** . */
      private Collection<String> scripts = Collections.emptyList();

      /** . */
      private Collection<String> stylesheets = Collections.emptyList();

      public Render addScript(String script) throws NullPointerException
      {
         if (script == null)
         {
            throw new NullPointerException("No null script accepted");
         }
         if (scripts.isEmpty())
         {
            scripts = new LinkedHashSet<String>();
         }
         scripts.add(script);
         return this;
      }

      public Render addStylesheet(String stylesheet) throws NullPointerException
      {
         if (stylesheet == null)
         {
            throw new NullPointerException("No null stylesheet accepted");
         }
         if (stylesheets.isEmpty())
         {
            stylesheets = new LinkedHashSet<String>();
         }
         stylesheets.add(stylesheet);
         return this;
      }

      public Collection<String> getScripts()
      {
         return scripts;
      }

      public Collection<String> getStylesheets()
      {
         return stylesheets;
      }

      public abstract String getTitle();

      @Override
      public String toString()
      {
         return "Response.Render[]";
      }
   }

   public static abstract class Resource extends Content
   {

      public abstract int getStatus();

      @Override
      public String toString()
      {
         return "Response.Resource[]";
      }
   }

   public static Response.Redirect redirect(String location)
   {
      return new Response.Redirect(location);
   }

   public static Content content(final String content)
   {
      return new Content()
      {
         public void send(Printer printer) throws IOException
         {
            printer.write(content);
         }
      };
   }

   public static Render render(String content)
   {
      return render(null, content);
   }

   public static Render render(final String title, final String content)
   {
      return new Render()
      {

         /** . */
         private String _title = title;

         @Override
         public String getTitle()
         {
            return _title;
         }

         public void send(Printer printer) throws IOException
         {
            if (content != null)
            {
               printer.write(content);
            }
         }
      };
   }

   public static Resource ok()
   {
      return ok(null);
   }

   public static Resource ok(String content)
   {
      return status(200, content);
   }

   public static Resource notFound()
   {
      return notFound(null);
   }

   public static Resource notFound(String content)
   {
      return status(404, content);
   }

   public static Resource status(int code)
   {
      return status(code, null);
   }

   public static Resource status(final int code, final String content)
   {
      return new Resource()
      {
         @Override
         public int getStatus()
         {
            return code;
         }

         public void send(Printer printer) throws IOException
         {
            if (content != null)
            {
               printer.write(content);
            }
         }
      };
   }
}
