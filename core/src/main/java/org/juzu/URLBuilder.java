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

import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.impl.utils.ParameterHashMap;
import org.juzu.impl.utils.ParameterMap;
import org.juzu.request.RequestContext;

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
public final class URLBuilder
{

   /** Escape XML type literal. */
   public static class ESCAPE_XML extends PropertyType<Boolean> {}

   /** Escape XML type literal instance. */
   public static ESCAPE_XML ESCAPE_XML = new ESCAPE_XML();

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final MimeBridge bridge;

   /** . */
   private final ControllerMethod method;

   /** . */
   private ParameterMap parameterMap;

   /** . */
   private Map<PropertyType<?>, Object> properties;

   public URLBuilder(MimeBridge bridge, ControllerMethod method)
   {
      HashMap<PropertyType<?>, Object> properties = new HashMap<PropertyType<?>, Object>();
      properties.put(RequestContext.METHOD_ID, method.getId());
      
      //
      this.bridge = bridge;
      this.method = method;
      this.parameterMap = new ParameterHashMap();
      this.properties = properties;
   }

   public URLBuilder setParameter(String name, String value) throws NullPointerException
   {
      parameterMap.setParameter(name, value);
      return this;
   }

   public URLBuilder setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException
   {
      parameterMap.setParameter(name, value);
      return this;
   }

   public URLBuilder setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException
   {
      parameterMap.setParameters(parameters);
      return this;
   }

   public URLBuilder escapeXML(Boolean escapeXML)
   {
      setProperty(ESCAPE_XML, escapeXML);
      return this;
   }

   /**
    * Set or clear a property of the URL.
    *
    * @param propertyType the property type
    * @param propertyValue the property value
    * @param <T> the property generic type
    * @return this URL builder
    * @throws IllegalArgumentException if the property is not valid
    */
   public <T> URLBuilder setProperty(PropertyType<T> propertyType, T propertyValue) throws IllegalArgumentException
   {
      if (propertyValue == null)
      {
         if (properties != null)
         {
            properties.remove(propertyType);
         }
      }
      else
      {
         String invalid = bridge.checkPropertyValidity(method.getPhase(), propertyType, propertyValue);
         if (invalid != null)
         {
            throw new IllegalArgumentException(invalid);
         }
         if (properties == null)
         {
            properties = new HashMap<PropertyType<?>, Object>();
         }
         properties.put(propertyType, propertyValue);
      }
      return this;
   }

   /**
    * Build the string value of this URL.
    *
    * @return the string url
    */
   public String toString()
   {
      return bridge.renderURL(method.getPhase(), parameterMap, properties);
   }
}
