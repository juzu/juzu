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

package plugin.jackson.response.node;

import juzu.MimeType;
import juzu.View;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @View
  @MimeType.JSON
  public TreeNode index() throws IOException {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    return factory.objectNode().set("foo", factory.textNode("bar"));
  }
}
