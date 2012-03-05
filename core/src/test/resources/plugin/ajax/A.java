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

package plugin.ajax;

import org.juzu.Controller;
import org.juzu.Resource;
import org.juzu.Response;
import org.juzu.View;
import org.juzu.plugin.ajax.Ajax;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller
{
   @View
   public Response.Render index()
   {
      String content = "" +
         "<script>\n" +
         "$(function() {\n" +
         "  $('#trigger').click(function() {\n" +
         "    doRequest(this.AjaxApplication().resource());\n" +
         "  });\n" +
         "  $('#trigger2').click(function() {\n" +
         "    doRequest(this.AjaxApplication().resource2());\n" +
         "  });\n" +
         "});\n" +

         "doRequest = function(url) {\n" +
         "  $.ajax({\n" +
         "    url:url,\n" +
         "    async:false,\n" +
         "    success:function(content) {\n" +
         "      alert(content);\n" +
         "    }\n" +
         "  });\n" +
         "}\n" +
         "</script>\n" +

         "<a id='trigger' href='#'>click</a>\n" +
         "<a id='trigger2' href='#'>click</a>";
      return Response.render(content).addScript(renderContext.getHttpContext().getContextPath() + "/jquery.js");
   }

   @Ajax
   @Resource
   public Response.Resource resource()
   {
      return Response.status(200, "OK MEN");
   }

   @Ajax
   @Resource
   public Response.Resource resource2()
   {
      return Response.status(200, "OK MEN 2");
   }
}
