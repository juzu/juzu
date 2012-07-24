/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package http.ajax;

import juzu.Controller;
import juzu.Resource;
import juzu.Response;
import juzu.View;
import juzu.asset.Asset;
import juzu.asset.AssetLocation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller {

  @View
  public Response.Content index(String p) {
    String content =
      "<script>\n" +
        "$(function() {\n" +
        "  $('#trigger').click(function() {\n" +
        "    $.ajax({\n" +
        "      url:'" + A_.resourceURL() + "',\n" +
        "      async: false,\n" +
        "      success: function(html) {\n" +
        "        $('#foo').html(html);\n" +
        "      }\n" +
        "    });\n" +
        "  });\n" +
        "});\n" +
        "</script>\n" +
        "<a id='trigger' href='#'>click</a>\n" +
        "<div id='foo'>foo</div>";
    return Response.render(content).addScript(Asset.uri(AssetLocation.SERVER, "jquery.js"));
  }

  @Resource
  public Response.Content resource() {
    return Response.ok("bar");
  }
}
