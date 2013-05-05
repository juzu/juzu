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
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.dialect.gtmpl.MessageKey;
import juzu.io.Streams;
import juzu.io.Stream;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderContext {

  /** . */
  private final Map<String, ?> attributes;

  /** . */
  private final Locale locale;

  /** . */
  private PropertyMap properties;

  /** . */
  protected Stream.Char printer;

  /** . */
  private final TemplateStub stub;

  public TemplateRenderContext(TemplateStub stub) {
    this(stub, Collections.<String, Object>emptyMap());
  }

  public TemplateRenderContext(TemplateStub stub, Map<String, ?> attributes) {
    this(stub, null, attributes, null);
  }

  public TemplateRenderContext(TemplateStub stub, Locale locale) {
    this(stub, null, Collections.<String, Object>emptyMap(), locale);
  }

  public TemplateRenderContext(TemplateStub stub, PropertyMap properties, Map<String, ?> attributes, Locale locale) {
    this.locale = locale;
    this.attributes = attributes;
    this.stub = stub;
    this.properties = properties;
  }

  public Map<String, ?> getAttributes() {
    return attributes;
  }

  public Locale getLocale() {
    return locale;
  }

  public Stream.Char getPrinter() {
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
    render(Streams.appendable(buffer));
    return buffer;
  }

  public void render(Stream.Char printer) throws IOException {
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
