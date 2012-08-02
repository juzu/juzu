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

package juzu.template;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.template.spi.TemplateStub;
import juzu.io.AppendableStream;
import juzu.io.Stream;

import java.io.IOException;
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

  public Object resolveBean(String expression) throws ApplicationException {
    return null;
  }

  public StringBuilder render() throws IOException {
    StringBuilder buffer = new StringBuilder();
    render(new AppendableStream(buffer));
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
