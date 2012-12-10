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

package juzu.impl.fs.spi.ram;

import java.util.LinkedHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class RAMDir extends RAMPath {

  /** . */
  final LinkedHashMap<String, RAMPath> children;

  /** . */
  long lastModified;

  RAMDir(RAMFileSystem fs) {
    super(fs);

    //
    this.children = new LinkedHashMap<String, RAMPath>();
    this.lastModified = System.currentTimeMillis();
  }

  RAMDir(RAMDir parent, String name, long lastModified) {
    super(parent, name);

    //
    this.children = new LinkedHashMap<String, RAMPath>();
    this.lastModified = lastModified;
  }

  @Override
  long getLastModified() {
    return lastModified;
  }

  @Override
  void touch() {
    this.lastModified = System.currentTimeMillis();
  }
}
