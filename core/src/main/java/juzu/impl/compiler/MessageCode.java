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

package juzu.impl.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MessageCode {

  /** . */
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  /** . */
  private static final ConcurrentHashMap<String, MessageCode> codes = new ConcurrentHashMap<String, MessageCode>();

  /**
   * Decode the message key and return a corresponding message code object. If no error can be decoded for the
   * specified key, null is returned.
   *
   * @param key the error key
   * @return the corresponding error
   */
  public static MessageCode decode(String key) {
    return codes.get(key);
  }

  /** . */
  private final String key;

  /** . */
  private final String message;

  public MessageCode(String key, String message) {
    codes.put(key, this);

    //
    this.key = key;
    this.message = message;
  }

  /**
   * The error key.
   *
   * @return the error key
   */
  public String getKey() {
    return key;
  }

  /**
   * The error message.
   *
   * @return the error message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "MessageCode[key=" + key + ",message=" + message + "]";
  }

  public ProcessingException failure(Object... args) {
    return new ProcessingException(this, args);
  }

  public ProcessingException failure(Element element) {
    return new ProcessingException(element, this, EMPTY_OBJECT_ARRAY);
  }

  public ProcessingException failure(Element element, Object... args) {
    return new ProcessingException(element, this, args);
  }

  public ProcessingException failure(Element element, AnnotationMirror annotation, Object... args) {
    return new ProcessingException(element, annotation, this, args);
  }
}
