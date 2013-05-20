/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package plugin.ajax;

import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {
  @View
  public Response.Content index() {
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

    return Response.ok(content);
  }

  @Ajax
  @Resource
  @Route("/m1")
  public Response.Body m1() {
    return Response.ok().body("m1()");
  }

  @Ajax
  @Resource
  @Route("/m2")
  public Response.Body m2(String p) {
    return Response.ok().body("m2(" + p + ")");
  }

  @Ajax
  @Resource
  @Route("/m3")
  public Response.Body m3() {
    return Response.ok().body("m3()");
  }
}
