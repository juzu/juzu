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

package juzu;

import org.w3c.dom.Element;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A property type describe a property associated with a generic type for providing type safetyness when
 * dealing with properties.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @param <T> the property generic type
 */
public abstract class PropertyType<T> {

  /** Asset. */
  public static PropertyType<String> ASSET = new PropertyType<String>(){};

  /** Title. */
  public static PropertyType<String> TITLE = new PropertyType<String>(){};

  /** Path. */
  public static final PropertyType<String> PATH = new PropertyType<String>(){};

  /** Redirect after action. */
  public static final PropertyType<Boolean> REDIRECT_AFTER_ACTION = new PropertyType<Boolean>(){};

  /** Header response. */
  public static final PropertyType<Map.Entry<String, String[]>> HEADER = new PropertyType<Map.Entry<String, String[]>>(){};

  /** Header tag. */
  public static final PropertyType<Element> HEADER_TAG = new PropertyType<Element>(){};

  /** Named meta tag . */
  public static final PropertyType<Map.Entry<String, String>> META_TAG = new PropertyType<Map.Entry<String, String>>(){};

  /** Http-equiv meta tag. */
  public static final PropertyType<Map.Entry<String, String>> META_HTTP_EQUIV = new PropertyType<Map.Entry<String, String>>(){};

  /** Mime type. */
  public static PropertyType<String> MIME_TYPE = new PropertyType<String>(){};

  /** Charset. */
  public static PropertyType<Charset> ENCODING = new PropertyType<Charset>(){};

  /** Escape XML. */
  public static PropertyType<Boolean> ESCAPE_XML = new PropertyType<Boolean>(){};

  protected PropertyType() throws NullPointerException {
  }

  public final T cast(Object o) {
    return (T)o;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this || obj != null && getClass().equals(obj.getClass());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
