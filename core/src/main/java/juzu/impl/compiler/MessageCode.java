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
