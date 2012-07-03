/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package juzu.impl.router.metadata;

import juzu.impl.router.EncodingMode;
import juzu.impl.router.QualifiedName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PathParamDescriptor extends ParamDescriptor {

  /** . */
  private String pattern;

  /** . */
  private EncodingMode encodingMode;

  /** . */
  private boolean captureGroup;

  public PathParamDescriptor(QualifiedName qualifiedName) {
    super(qualifiedName);

    //
    this.encodingMode = EncodingMode.FORM;
    this.captureGroup = false;
  }

  public PathParamDescriptor(String qualifiedName) {
    super(qualifiedName);

    //
    this.encodingMode = EncodingMode.FORM;
  }

  public PathParamDescriptor matchedBy(String pattern) {
    this.pattern = pattern;
    return this;
  }

  public PathParamDescriptor encodedBy(EncodingMode encodingMode) {
    this.encodingMode = encodingMode;
    return this;
  }

  public PathParamDescriptor captureGroup(boolean capture) {
    this.captureGroup = capture;
    return this;
  }

  public PathParamDescriptor preservePath() {
    return encodedBy(EncodingMode.PRESERVE_PATH);
  }

  public PathParamDescriptor form() {
    return encodedBy(EncodingMode.FORM);
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public EncodingMode getEncodingMode() {
    return encodingMode;
  }

  public void setEncodingMode(EncodingMode encodingMode) {
    this.encodingMode = encodingMode;
  }

  public boolean getCaptureGroup() {
    return captureGroup;
  }

  public void setCaptureGroup(boolean captureGroup) {
    this.captureGroup = captureGroup;
  }
}
