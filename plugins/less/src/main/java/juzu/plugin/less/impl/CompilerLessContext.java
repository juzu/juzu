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

package juzu.plugin.less.impl;

import juzu.impl.common.FileKey;
import juzu.impl.common.Name;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Path;
import juzu.plugin.less.impl.lesser.LessContext;

import javax.tools.FileObject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CompilerLessContext implements LessContext {

  /** . */
  final ProcessingContext processingContext;

  /** . */
  final ElementHandle.Package context;

  /** . */
  final Name pkg;

  CompilerLessContext(
    ProcessingContext processingContext,
    ElementHandle.Package context,
    Name pkg ) {
    this.processingContext = processingContext;
    this.context = context;
    this.pkg = pkg;
  }

  public String load(String ref) {
    try {
      Path.Absolute path = pkg.resolve(ref);
      FileObject c = processingContext.resolveResource(context, path);
      if (c != null) {
        try {
          return c.getCharContent(true).toString();
        }
        catch (IOException e) {
          processingContext.log("Could not get content of " + path, e);
        }
      }
    }
    catch (IllegalArgumentException e) {
      // Log ?
    }

    //
    return null;
  }
}
