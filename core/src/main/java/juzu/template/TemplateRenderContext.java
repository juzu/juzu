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

package juzu.template;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.common.Tools;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.dialect.gtmpl.MessageKey;
import juzu.io.OutputStream;
import juzu.io.Stream;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderContext {

  /** . */
  private Map<String, Object> attributes;

  /** . */
  private final Locale locale;

  /** . */
  private PropertyMap properties;

  /** . */
  protected Stream printer;

  /** . */
  private final TemplateStub stub;

  public TemplateRenderContext(TemplateStub stub) {
    this(stub, (Map<String, Object>)null);
  }

  public TemplateRenderContext(TemplateStub stub, Map<String, Object> attributes) {
    this(stub, null, attributes, null);
  }

  public TemplateRenderContext(TemplateStub stub, Locale locale) {
    this(stub, null, null, locale);
  }

  public TemplateRenderContext(TemplateStub stub, PropertyMap properties, Map<String, Object> attributes, Locale locale) {
    this.locale = locale;
    this.attributes = attributes;
    this.stub = stub;
    this.properties = properties;
  }

  /**
   * Renders a tag.
   *
   * @param name the tag name
   * @param body the tag body
   * @param parameters the tag paremeters
   */
  public void renderTag(String name, Renderable body, Map<String, String> parameters) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Map<String, ?> getAttributes() {
    return attributes;
  }

  public Object getAttribute(String name) {
    if (name == null) {
      throw new NullPointerException("No null attribute name accepted");
    }
    return attributes != null ? attributes.get(name) : null;
  }

  public Object setAttribute(String name, Object value) {
    if (name == null) {
      throw new NullPointerException("No null attribute name accepted");
    }
    if (value != null) {
      if (attributes == null) {
        attributes = new HashMap<String, Object>();
      }
      return attributes.put(name, value);
    } else {
      if (attributes != null) {
        return attributes.remove(name);
      } else {
        return null;
      }
    }
  }

  public Locale getLocale() {
    return locale;
  }

  public Stream getPrinter() {
    return printer;
  }

  public void setTitle(String title) {
    if (properties != null) {
      properties.setValue(PropertyType.TITLE, title);
    }
  }

  public TemplateStub resolveTemplate(String path) {
    return null;
  }

  public Object resolveBean(String expression) throws InvocationTargetException {
    return null;
  }

  public String resolveMessage(MessageKey key) {
    return key.toString();
  }

  public StringBuilder render() throws IOException {
    StringBuilder buffer = new StringBuilder();
    OutputStream consumer = OutputStream.create(Tools.UTF_8, buffer);
    render(consumer);
    return buffer;
  }

  public void render(Stream printer) throws IOException {
    if (this.printer != null) {
      throw new IllegalStateException("Already rendering");
    }

    //
    this.printer = printer;

    //
    try {
      stub.render(this);
    }
    finally {
      this.printer = null;
    }
  }
}
