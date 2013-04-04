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

package juzu.impl.plugin.template.metadata;

import juzu.Path;
import juzu.impl.template.spi.TemplateStub;
import juzu.template.Template;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateDescriptor {

  /** . */
  private final String path;

  /** . */
  private final Class<? extends Template> type;

  /** . */
  private final Class<? extends TemplateStub> stubType;

  public TemplateDescriptor(
      Class<? extends Template> type,
      Class<? extends TemplateStub> stubType) {
    Path path = type.getAnnotation(Path.class);

    //
    this.path = path.value();
    this.type = type;
    this.stubType = stubType;
  }

  public String getPath() {
    return path;
  }

  public Class<? extends Template> getType() {
    return type;
  }

  public Class<? extends TemplateStub> getStubType() {
    return stubType;
  }
}
