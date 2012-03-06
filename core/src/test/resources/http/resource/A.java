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

package http.resource;

import org.juzu.Response;
import org.juzu.View;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class A
{
   
   @View
   public Response.Content index(String p)
   {
      return Response.render("foo")
          .addScript("assets/jquery-1.7.1.js")
          .addStylesheet("assets/main.css")
          .addStylesheet("assets/main.less");
   }
}
