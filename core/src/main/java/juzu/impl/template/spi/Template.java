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

  /** The origin path. */
  private final Path.Relative origin;

  /** . */
  private final M model;

  /** . */
  private final Path.Relative relativePath;

  /** . */
  private final Path.Absolute absolutePath;

  /** . */
  private final LinkedHashSet<String> parameters;

  /** The last modified date. */
  private long lastModified;

  public Template(
    Path.Relative origin,
    M model,
    Path.Relative relativePath,
    Path.Absolute absolutePath,
    long lastModified) {
    this.origin = origin;
    this.model = model;
    this.relativePath = relativePath;
    this.parameters = new LinkedHashSet<String>();
    this.lastModified = lastModified;
    this.absolutePath = absolutePath;
  }

  public Path.Relative getOrigin() {
    return origin;
  }

  public Path.Absolute getAbsolutePath() {
    return absolutePath;
  }

  public Path.Relative getRelativePath() {
    return relativePath;
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
