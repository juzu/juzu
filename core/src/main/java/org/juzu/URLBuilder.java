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
public interface URLBuilder
{

   /**
    * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the given
    * name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
    *
    * @param name the parameter name
    * @param value the parameter value
    * @return this builder
    * @throws NullPointerException if the name parameter is null
    */
   URLBuilder setParameter(String name, String value) throws NullPointerException;

   /**
    * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the given
    * name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
    *
    * @param name the parameter name
    * @param value the parameter value
    * @return this builder
    * @throws NullPointerException if the name parameter is null
    */
   URLBuilder setParameter(String name, String[] value) throws NullPointerException;

   URLBuilder escapeXML(Boolean escapeXML);

   /**
    * Build the string value of this URL.
    *
    * @return the string url
    */
   String toString();

}
