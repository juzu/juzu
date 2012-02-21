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

import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.metadata.ControllerMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>The <code>URLBuilder</code> produces URL for a Juzu application.
 *
 * <p>Builders can be obtained from a {@link org.juzu.request.MimeContext} object for building controller methods, however
 * the favorite way to obtain a builder is to use a controller companion that provides a type safe way for creating
 * fully configured builders.</p>
 *
 * <p>Type safe <code>URLBuilder</code> factory method are generated for each view, action or resource controller methods.
 * The signature of an url builder factory is obtained by translating the signature of the controller method and appending
 * the suffix <i>URL</i> after the method name.</p>
 *
 * <code><pre>
 *    public void MyController {
 *
 *       &#064;Action
 *       public void myAction(String param) { }
 *
 *       &#064;View
 *       public void myRender() {
 *          URLBuilder builder = MyController_.myActionURL("hello");
 *          String url = builder.toString();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class URLBuilder
{

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final MimeBridge<?> bridge;

   /** . */
   private final ControllerMethod method;

   /** . */
   private Map<String, String[]> parameters;

   /** . */
   private Boolean escapeXML;

   public URLBuilder(MimeBridge<?> bridge, ControllerMethod method)
   {
      HashMap<String, String[]> parameters = new HashMap<String, String[]>();
      parameters.put("juzu.op", new String[]{method.getId()});
      
      //
      this.bridge = bridge;
      this.method = method;
      this.escapeXML = null;
      this.parameters = parameters;
   }

   /**
    * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the given
    * name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
    *
    * @param name the parameter name
    * @param value the parameter value
    * @return this builder
    * @throws NullPointerException if the name parameter is null
    */
   public URLBuilder setParameter(String name, String value) throws NullPointerException
   {
      return setParameter(name, value == null ? EMPTY_STRING_ARRAY : new String[]{value});
   }

   /**
    * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the given
    * name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
    *
    * @param name the parameter name
    * @param value the parameter value
    * @return this builder
    * @throws NullPointerException if the name parameter is null
    * @throws IllegalArgumentException if any component of the value is null
    */
   public URLBuilder setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (value == null)
      {
         throw new NullPointerException();
      }
      if (name.startsWith("juzu."))
      {
         throw new IllegalArgumentException("Parameter name cannot be prefixed with juzu.");
      }
      if (value.length == 0)
      {
         parameters.remove(name);
      }
      else
      {
         for (String component : value)
         {
            if (component == null)
            {
               throw new IllegalArgumentException("Argument array cannot contain null value");
            }
         }
         parameters.put(name, value.clone());
      }
      return this;
   }

   public URLBuilder escapeXML(Boolean escapeXML)
   {
      this.escapeXML = escapeXML;
      return this;
   }

   /**
    * Build the string value of this URL.
    *
    * @return the string url
    */
   public String toString()
   {
      return bridge.renderURL(method.getPhase(), escapeXML, parameters);
   }
}
