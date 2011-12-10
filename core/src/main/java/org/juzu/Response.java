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
 * <p>A response object signalling to the portal the action to take after an interaction. This object is usually returned
 * after the invocation of a {@link Action} controller and instructs the aggregator the action to take.</p>
 * 
 * <p>A <code>Response.Redirect</code> response instructs the aggregator to make a redirection to a valid URL after the 
 * interaction, this kind of response is created using an {@link org.juzu.request.ActionContext}:
 * <code><pre>
 *    return context.redirect("http://www.exoplatform.org");
 * </pre></code>
 * </p>
 * 
 * <p>A <code>Response.Render</code> response instructs the aggreator to proceed to the render phase of a valid
 * view controller, this kind of response can be created using an {@link org.juzu.request.ActionContext}, however
 * the the preferred way is to use a controller companion class that carries method factories for creating render
 * responses.</p>
 *
 * <p>Type safe <code>Response.Render</code> factory method are generated for each view or resource controller methods.
 * The signature of an render factory is obtained by using the same signature of the controller method.</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Action
 *       public Reponse.Render myAction() {
 *          return MyController_.myRender("hello");
 *     }
 *
 *     &#064;View
 *       public void myRender(String param) {
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> 
 */
public interface Response
{

   /**
    * A response instructing to execute a render phase of a controller method after the current interaction.
    */
   interface Render extends Response
   {

      /**
       * Set a parameter for the controller method.
       *
       * @param parameterName the parameter name
       * @param parameterValue the parameter value
       * @return this object
       */
      Render setParameter(String parameterName, String parameterValue);

   }

   /**
    * A response instructing to execute an HTTP redirection after the current interaction.
    */
   interface Redirect extends Response
   {
   }
}
