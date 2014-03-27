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
package juzu.plugin.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.EntityUnmarshaller;
import juzu.request.ClientContext;
import juzu.request.RequestParameter;

import java.io.IOException;
import java.util.Map;

/**
 * An {@link juzu.impl.request.EntityUnmarshaller} for Json using the Jackson framework.
 *
 * @author Julien Viet
 */
public class JacksonUnmarshaller extends EntityUnmarshaller {

  @Override
  public boolean accept(String mediaType) {
    return mediaType.equals("application/json");
  }

  @Override
  public void unmarshall(
      String mediaType,
      ClientContext context,
      Iterable<Map.Entry<ContextualParameter, Object>> contextualArguments,
      Map<String, RequestParameter> parameterArguments) throws IOException {

    //
    ObjectMapper mapper = new ObjectMapper();

    // Parse the payload first
    JsonNode tree = mapper.readTree(context.getInputStream());

    for (Map.Entry<ContextualParameter, Object> contextualArgument : contextualArguments) {
      Class<?> type = contextualArgument.getKey().getType();
      if (JsonNode.class.isAssignableFrom(type)) {
        contextualArgument.setValue(tree);
      } else if (type.getAnnotation(Jackson.class) != null) {
        Object value = mapper.readValue(new TreeTraversingParser(tree), type);
        contextualArgument.setValue(value);
      }
    }
  }
}
