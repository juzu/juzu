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

import juzu.impl.common.Location;

/**
 * An exception that denotes a template execution exception.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateExecutionException extends RuntimeException {

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