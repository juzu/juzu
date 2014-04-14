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

import juzu.io.Streamable;

import java.lang.reflect.AnnotatedElement;

/**
 * The entity unmarshaller is used by Juzu for translating an object into an HTTP entity.
 *
 * @author Julien Viet
 */
// tag::class[]
public abstract class EntityMarshaller {

  /**
   * Marshall the object for the specified <code>mimeType</code> or return null.
   *
   * @param mimeType the mime type to test
   * @param annotations the contextual annotations
   * @param object the object to marshall  @return the corresponding streamable
   */
  public abstract Streamable marshall(String mimeType, AnnotatedElement annotations, Object object);

}
// end::class[]