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

import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.common.Location;

/**
 * An exception that denotes a template execution exception.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateExecutionException extends ApplicationException {

  /** . */
  private final String templateId;

  /** . */
  private final Location location;

  /** . */
  private final String text;

  public TemplateExecutionException(String templateId, Location location, String text, String message, Throwable cause) {
    super(message, cause);

    //
    this.templateId = templateId;
    this.location = location;
    this.text = text;
  }

  public TemplateExecutionException(String templateId, Location location, String text, Throwable cause) {
    super(cause);

    //
    this.templateId = templateId;
    this.location = location;
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public Integer getLineNumber() {
    return text != null ? location.getLine() : null;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder("Groovy template exception");
    if (location != null) {
      sb.append(" at ").append(location);
    }
    if (text != null) {
      sb.append(" script ").append(text);
    }
    if (templateId != null) {
      sb.append(" for template ").append(templateId);
    }
    return sb.toString();
  }
}