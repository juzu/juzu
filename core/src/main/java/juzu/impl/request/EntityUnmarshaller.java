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
package juzu.impl.request;

import juzu.request.ClientContext;
import juzu.request.RequestParameter;

import java.io.IOException;
import java.util.Map;

/**
 * The entity unmarshaller is used by Juzu for translating an HTTP entity into a request control arguments.
 *
 * @author Julien Viet
 */
// tag::class[]
public abstract class EntityUnmarshaller {

  /**
   * Decide wether or not this reader accept to read the specified <code>mediaType</code> argument.
   *
   * @param mediaType the media type to test
   * @return true if the media type is accepted
   */
  public abstract boolean accept(String mediaType);

  /**
   * Unmarshall the entity from the {@link juzu.request.ClientContext}. This unmarshaller can modify the
   * request control arguments:
   * <ul>
   *   <li>the contextual arguments can be iterated and the value updated</li>
   *   <li>the parameter arguments map can be freely modified</li>
   * </ul>
   *
   * @param mediaType the request media type
   * @param context the client context for reading the entity
   * @param contextualArguments the contextual arguments
   * @param parameterArguments the contextual parameters
   * @throws IOException anything preventing the read operation to succeed
   */
  public abstract void unmarshall(
      String mediaType,
      ClientContext context,
      Iterable<Map.Entry<ContextualParameter, Object>> contextualArguments,
      Map<String, RequestParameter> parameterArguments) throws IOException;

}
// end::class[]
