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

package juzu.impl.router;

import juzu.impl.common.QualifiedName;

/**
 * Various predefined names for testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface Names {

  QualifiedName Q_A = QualifiedName.parse("q:a");
  QualifiedName A = QualifiedName.parse("a");
  QualifiedName B = QualifiedName.parse("b");
  QualifiedName P = QualifiedName.parse("p");
  QualifiedName FOO = QualifiedName.parse("foo");
  QualifiedName BAR = QualifiedName.parse("bar");
  QualifiedName JUU = QualifiedName.parse("juu");
  QualifiedName GTN_HANDLER = QualifiedName.parse("gtn:handler");
  QualifiedName GTN_LANG = QualifiedName.parse("gtn:lang");
  QualifiedName GTN_SITENAME = QualifiedName.parse("gtn:sitename");
  QualifiedName GTN_SITETYPE = QualifiedName.parse("gtn:sitetype");
  QualifiedName GTN_ACCESS = QualifiedName.parse("gtn:access");
  QualifiedName GTN_PATH = QualifiedName.parse("gtn:path");
  QualifiedName GTN_MIN = QualifiedName.parse("gtn:min");
}
