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

import org.juzu.impl.utils.Tools;
import org.juzu.io.BinaryStream;
import org.juzu.io.CharStream;
import org.juzu.io.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

   /** . */
   private static final Map EMPTY_MAP = Collections.emptyMap();
   
   /**
    * A response instructing to execute a render phase of a controller method after the current interaction.
    */
   public static class Update extends Response
   {

      /** . */
      private Map<String, String[]> parameters;

      /** . */
      private Map<PropertyType<?>, Object> properties;

      public Update()
      {
         this.parameters = EMPTY_MAP;
         this.properties = EMPTY_MAP;
      }

      /**
       * Set a parameter, if the value is null, the parameter is removed.
       *
       * @param parameterName the parameter name
       * @param parameterValue the parameter value
       * @return this object
       * @throws NullPointerException if the paraemter name is null
       */
      public Update setParameter(String parameterName, String parameterValue) throws NullPointerException
      {
         return setParameter(parameterName, new String[]{parameterValue});
      }

      /**
       * Set a parameter, if the value is null, the parameter is removed.
       *
       * @param parameterName the parameter name
       * @param parameterValues the parameter value
       * @return this object
       * @throws NullPointerException if the paraemter name is null
       */
      public Update setParameter(String parameterName, String[] parameterValues) throws NullPointerException
      {
         if (parameterName == null)
         {
            throw new NullPointerException();
         }
         if (parameterName.startsWith("juzu."))
         {
            throw new IllegalArgumentException("Parameter name cannot start with <juzu.> prefix");
         }
         if (parameterValues != null)
         {
            if (parameters == EMPTY_MAP)
            {
               parameters = new HashMap<String, String[]>();
            }
            parameters.put(parameterName, parameterValues);
         }
         else
         {
            if (parameters.size() > 0)
            {
               parameters.remove(parameterName);
            }
         }
         return this;
      }

      /**
       * Set all parameters, if the entry value is null, the parameter is removed.
       *
       * @param parameters the parameters
       * @return this object
       * @throws NullPointerException if the paraemter name is null
       */
      public Update setAllParameters(Map<String, String[]> parameters) throws NullPointerException
      {
         for (String key : parameters.keySet())
         {
            setParameter(key, parameters.get(key));
         }
         return this;
      }

      /**
       * Set a property, if the value is null, the property is removed.
       *
       * @param propertyType the property type
       * @param propertyValue the property value
       * @return this object
       * @throws NullPointerException if the property type is null
       */
      public <T> Update setProperty(PropertyType<T> propertyType, T propertyValue)
      {
         if (propertyType == null)
         {
            throw new NullPointerException("No null property type allowed");
         }
         if (propertyValue != null)
         {
            if (properties == EMPTY_MAP)
            {
               properties = new HashMap<PropertyType<?>, Object>();
            }
            properties.put(propertyType, propertyValue);
         }
         else
         {
            if (properties.size() > 0)
            {
               properties.remove(propertyType);
            }
         }
         return this;
      }

      public Map<String, String[]> getParameters()
      {
         return parameters;
      }

      public Map<PropertyType<?>, ?> getProperties()
      {
         return properties;
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
            if (parameters.keySet().equals(that.parameters.keySet()))
            {
               for (Map.Entry<String, String[]> entry : parameters.entrySet())
               {
                  String[] value = that.parameters.get(entry.getKey());
                  if  (!Arrays.equals(entry.getValue(), value))
                  {
                     return false;
                  }
               }
            }
            else
            {
               return false;
            }
            return properties.equals(that.properties);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "Response.Update[parameters" + parameters + ",properties=" + properties + "]";
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

   public static abstract class Content<S extends Stream> extends Response
   {
      
      public abstract Class<S> getKind();
      
      public String getMimeType()
      {
         // No specific mime type yet
         return null;
      }

      /**
       * Send the response on the stream argument, Juzu invokes it when it needs to render the content object.
       *
       * @param stream the stream for sending the response
       * @throws IOException any io exception
       */
      public void send(S stream) throws IOException
      {
         // Do nothing
      }
   }

   public static class Render extends Content<CharStream>
   {

      public Iterator<String> getScripts()
      {
         return Tools.emptyIterator();
      }

      public Iterator<String> getStylesheets()
      {
         return Tools.emptyIterator();
      }

      public String getTitle()
      {
         return null;
      }

      @Override
      public String getMimeType()
      {
         return "text/html";
      }

      @Override
      public Class<CharStream> getKind()
      {
         return CharStream.class;
      }

      @Override
      public String toString()
      {
         return "Response.Render[]";
      }

      public static class Base extends Render
      {

         /** . */
         private String title;
         
         /** . */
         private Collection<String> scripts = Collections.emptyList();

         /** . */
         private Collection<String> stylesheets = Collections.emptyList();

         public Base(String title)
         {
            this.title = title;
         }

         public Base()
         {
         }

         @Override
         public String getTitle()
         {
            return title;
         }

         public void setTitle(String title)
         {
            this.title = title;
         }

         @Override
         public Iterator<String> getScripts()
         {
            return scripts.iterator();
         }

         @Override
         public Iterator<String> getStylesheets()
         {
            return stylesheets.iterator();
         }

         public Base addScript(String script) throws NullPointerException
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

         public Base addStylesheet(String stylesheet) throws NullPointerException
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
      }
   }

   public static abstract class Resource<S extends Stream> extends Content<S>
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

   public static Content<CharStream> content(final String content)
   {
      return new Content<CharStream>()
      {
         @Override
         public Class<CharStream> getKind()
         {
            return CharStream.class;
         }

         public void send(CharStream stream) throws IOException
         {
            stream.append(content);
         }
      };
   }

   public static Render.Base render(String content)
   {
      return render(null, content);
   }

   public static Render.Base render(final String title, final String content)
   {
      return new Render.Base(title)
      {
         public void send(CharStream stream) throws IOException
         {
            if (content != null)
            {
               stream.append(content);
            }
         }
      };
   }

   public static Resource<?> ok()
   {
      return ok((String)null);
   }

   public static Resource<CharStream> ok(String content)
   {
      return status(200, content);
   }

   public static Resource<BinaryStream> ok(String mimeType, InputStream content)
   {
      return status(200, mimeType, content);
   }

   public static Resource<BinaryStream> ok(InputStream content)
   {
      return ok(null, content);
   }

   public static Resource<?> notFound()
   {
      return notFound(null);
   }

   public static Resource<CharStream> notFound(String content)
   {
      return status(404, content);
   }

   public static Resource<?> status(int code)
   {
      return status(code, (String)null);
   }

   public static Resource<CharStream> status(final int code, final String content)
   {
      return new Resource<CharStream>()
      {
         @Override
         public String getMimeType()
         {
            return "text/html";
         }

         @Override
         public Class<CharStream> getKind()
         {
            return CharStream.class;
         }

         @Override
         public int getStatus()
         {
            return code;
         }

         public void send(CharStream stream) throws IOException
         {
            if (content != null)
            {
               stream.append(content);
            }
         }
      };
   }

   public static Resource<BinaryStream> status(int code, InputStream content)
   {
      return status(code, null, content);
   }

   public static Resource<BinaryStream> status(final int code, final String mimeType, final InputStream content)
   {
      return new Resource<BinaryStream>()
      {
         @Override
         public Class<BinaryStream> getKind()
         {
            return BinaryStream.class;
         }

         @Override
         public String getMimeType()
         {
            return mimeType;
         }

         @Override
         public int getStatus()
         {
            return code;
         }

         @Override
         public void send(BinaryStream stream) throws IOException
         {
            if (content != null)
            {
               byte[] buffer = new byte[256];
               for (int l;(l = content.read(buffer)) != -1;)
               {
                  stream.append(buffer, 0, l);
               }
            }
         }
      };
   }
}
