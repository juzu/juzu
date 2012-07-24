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

package juzu.plugin.less.impl;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Content;
import juzu.impl.common.Path;
import juzu.impl.common.QN;
import juzu.plugin.less.impl.lesser.LessContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CompilerLessContext implements LessContext {

  /** . */
  final ProcessingContext processingContext;

  /** . */
  final ElementHandle.Package context;

  /** . */
  final QN pkg;

  /** . */
  final Path.Absolute pkgPath;

  CompilerLessContext(
    ProcessingContext processingContext,
    ElementHandle.Package context,
    QN pkg ) {
    this.processingContext = processingContext;
    this.context = context;
    this.pkg = pkg;
    this.pkgPath = Path.Absolute.create(pkg, "", "");
  }

  public String load(String ref) {
    try {
      Path.Absolute path = pkgPath.append(ref);
      Content c = processingContext.resolveResource(context, path);
      if (c != null) {
        return c.getCharSequence().toString();
      }
    }
    catch (IllegalArgumentException e) {
      // Log ?
    }

    //
    return null;
  }
}
