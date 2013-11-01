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
package juzu.plugin.less4j.impl;

import com.github.sommeri.less4j.LessSource;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;

import javax.tools.FileObject;
import java.io.IOException;

/** @author Julien Viet */
public class CompilerLessSource extends LessSource {

  /** . */
  private final ProcessingContext processingContext;

  /** . */
  private final ElementHandle.Package context;

  /** . */
  private final Path.Absolute path;

  /** . */
  private String content;

  public CompilerLessSource(ProcessingContext processingContext, ElementHandle.Package context, Path.Absolute path) {
    this.processingContext = processingContext;
    this.context = context;
    this.path = path;
  }

  @Override
  public LessSource relativeSource(String s) throws FileNotFound, CannotReadFile, StringSourceException {
    Path.Absolute resolvedPath = path.getDirs().resolve(s);
    return new CompilerLessSource(processingContext, context, resolvedPath);
  }

  @Override
  public String getContent() throws FileNotFound, CannotReadFile {
    if (content == null) {
      FileObject c = processingContext.resolveResourceFromSourcePath(context, path);
      if (c != null) {
        try {
          content = c.getCharContent(true).toString();
        }
        catch (Exception e) {
          processingContext.error("Could not get content of " + path, e);
          CannotReadFile cannotReadFile = new CannotReadFile();
          cannotReadFile.initCause(e);
          throw cannotReadFile;
        }
      } else {
        throw new FileNotFound();
      }
    }
    return content;
  }
}
