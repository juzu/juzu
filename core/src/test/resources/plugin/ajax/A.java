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

package plugin.ajax;

import juzu.Controller;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller {
  @View
  public Response.Render index() {
    String content = "" +

      //
      "<script>\n" +
      "$(function() {\n" +
      "  $('#trigger1').click(function() {\n" +
      "    $(this).jzAjax({\n" +
      "      url:'A.m1()',\n" +
      "      success:function(content) {\n" +
      "        alert(content);\n" +
      "      }\n" +
      "    });\n" +
      "  });\n" +
      "});\n" +
      "</script>\n" +
      "<a id='trigger1' href='#'>click</a>\n" +

      //
      "<script>\n" +
      "$(function() {\n" +
      "  $('#trigger2').click(function() {\n" +
      "    $(this).jzAjax({\n" +
      "      url:'A.m2()',\n" +
      "      data:{p:'foo'},\n" +
      "      success:function(content) {\n" +
      "        alert(content);\n" +
      "      }\n" +
      "    });\n" +
      "  });\n" +
      "});\n" +
      "</script>\n" +
      "<a id='trigger2' href='#'>click</a>\n" +

      //
      "<div id='target' href='#'>click</div>\n" +
      "<script>\n" +
      "$(function() {\n" +
      "  $('#trigger3').click(function() {\n" +
      "    var a = $('#target').jzLoad('A.m3()', function(data) { alert(data); });\n" +
      "  });\n" +
      "});\n" +
      "</script>\n" +
      "<a id='trigger3' href='#'>click</a>" +

      // Configure ajax default for unit test
      "<script>\n" +
      "$(function() {\n" +
      "$.ajaxSetup({\n" +
      "async:false\n" +
      "});\n" +
      "});\n" +
      "</script>\n";

    return Response.render(content);
  }

  @Ajax
  @Resource
  @Route("/m1")
  public Response.Content<?> m1() {
    return Response.content(200, "m1()");
  }

  @Ajax
  @Resource
  @Route("/m2")
  public Response.Content<?> m2(String p) {
    return Response.content(200, "m2(" + p + ")");
  }

  @Ajax
  @Resource
  @Route("/m3")
  public Response.Content<?> m3() {
    return Response.content(200, "m3()");
  }
}
