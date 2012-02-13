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
import java.util.HashMap;
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
 * <p>A <code>Response.Process.Action.Render</code> response instructs the aggreator to proceed to the render phase of a valid
 * view controller, this kind of response can be created using an {@link org.juzu.request.ActionContext}, however
 * the the preferred way is to use a controller companion class that carries method factories for creating render
 * responses.</p>
 *
 * <p>Type safe {@link Response.Action.Render} factory method are generated for each view or resource controller methods.
 * The signature of an render factory is obtained by using the same signature of the controller method.</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Action
 *       public {@link Response.Action.Render} myAction() {
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
 * <p>The {@link Mime} class is the base response class which will work well for the two phases. However the 
 * {@link org.juzu.request.Phase#RENDER} can specify an optional title and the {@link org.juzu.request.Phase#RESOURCE} can
 * specify an optional status code for the user agent response.</p>
 * 
 * <p>Responses are created using the {@link Response} factory methods such as</p>
 * 
 * <ul>
 *    <li>{@link Response#ok} creates an ok response</li>
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
 *       public {@link Response.Mime} myView() {
 *          return index.ok();
 *       }
 *
 *       &#064;Inject &#064;Path("error.gtmpl")  {@link org.juzu.template.Template} error;
 *
 *       &#064;Resource
 *       public {@link Response.Mime} myView() {
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
 *       public {@link Response.Mime} myView() {
 *          return index.with().label("hello").ok();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Response
{

   public interface Update
   {
      String getMethodId();
      Update setParameter(String parameterName, String parameterValue);
      Map<String, String> getParameters();
   }

   public interface Stream
   {
      void send(Printer printer) throws IOException;
   }

   public static class Action extends Response
   {

      /**
       * A response instructing to execute a render phase of a controller method after the current interaction.
       */
      public static class Render extends Action implements Update
      {

         /** . */
         final String methodId;

         /** . */
         final Map<String, String> parameters;

         public Render(String methodId)
         {
            this.methodId = methodId;
            this.parameters = new HashMap<String, String>();
         }

         /**
          * Set a parameter for the controller method.
          *
          * @param parameterName the parameter name
          * @param parameterValue the parameter value
          * @return this object
          */
         public Render setParameter(String parameterName, String parameterValue)
         {
            if (parameterName == null)
            {
               throw new NullPointerException();
            }
            if (parameterValue == null)
            {
               throw new NullPointerException();
            }
            this.parameters.put(parameterName, parameterValue);
            return this;
         }

         public Map<String, String> getParameters()
         {
            return parameters;
         }

         public String getMethodId()
         {
            return methodId;
         }

         @Override
         public boolean equals(Object obj)
         {
            if (obj == this)
            {
               return true;
            }
            if (obj instanceof Render)
            {
               Render that = (Render)obj;
               return methodId.equals(that.methodId) && parameters.equals(that.parameters);
            }
            return false;
         }

         @Override
         public String toString()
         {
            return "Response.Action.Render[methodId=" + methodId + ",parameters" + parameters + "]";
         }
      }

      /**
       * A response instructing to execute an HTTP redirection after the current interaction.
       */
      public static class Redirect extends Action
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
      }
   }
   
   public static abstract class Mime extends Response implements Stream
   {
   }

   public static abstract class Render extends Mime
   {
      public abstract String getTitle();
   }

   public static abstract class Resource extends Mime
   {
      public abstract int getStatus();
   }

   public static Response.Action.Redirect redirect(String location)
   {
      return new Response.Action.Redirect(location);
   }

   public static Mime ok(final String content)
   {
      return new Mime()
      {
         public void send(Printer printer) throws IOException
         {
            printer.write(content);
         }
      };
   }

   public static Mime.Render ok(final String title, final String content)
   {
      return new Mime.Render()
      {
         @Override
         public String getTitle()
         {
            return title;
         }
         public void send(Printer printer) throws IOException
         {
            printer.write(content);
         }
      };
   }

   public static Mime.Resource notFound()
   {
      return notFound(null);
   }

   public static Mime.Resource notFound(final String content)
   {
      return status(404, content);
   }

   public static Mime.Resource status(final int code)
   {
      return status(code, null);
   }

   public static Mime.Resource status(final int code, final String content)
   {
      return new Mime.Resource()
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
