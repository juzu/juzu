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

package juzu.impl.asset;

import juzu.asset.AssetLocation;

import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetNode {

  /** . */
  private static final Set<String> EMPTY_SET = Collections.emptySet();

  /** . */
  final String id;

  /** . */
  final AssetLocation location;

  /** . */
  final String value;

  /** . */
  Set<String> dependsOnMe;

  /** . */
  Set<String> iDependOn;

  AssetNode(String id, AssetLocation location, String value, Set<String> iDependOn) {
    this.id = id;
    this.location = location;
    this.value = value;
    this.dependsOnMe = EMPTY_SET;
    this.iDependOn = iDependOn;
  }

  public String getId() {
    return id;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getValue() {
    return value;
  }
}
