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

package juzu.impl.template.spi;

import juzu.impl.common.Path;

import java.io.Serializable;
import java.util.LinkedHashSet;

/**
 * The representation of a compilable template in a context.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @param <M> the template model
 */
public class Template<M extends Serializable> implements Serializable {

  /** . */
  private final M model;

  /** . */
  private final Path.Absolute path;

  /** . */
  private final LinkedHashSet<String> parameters;

  /** The last modified date. */
  private final long lastModified;

  /** . */
  private final long md5;

  public Template(
    M model,
    Path.Absolute path,
    long lastModified,
    long md5) {
    this.model = model;
    this.parameters = new LinkedHashSet<String>();
    this.lastModified = lastModified;
    this.path = path;
    this.md5 = md5;
  }

  public Path.Absolute getPath() {
    return path;
  }

  public long getMD5() {
    return md5;
  }

  public M getModel() {
    return model;
  }

  public long getLastModified() {
    return lastModified;
  }

  public LinkedHashSet<String> getParameters() {
    return parameters;
  }

  public void addParameter(String parameterName) {
    parameters.add(parameterName);
  }
}
