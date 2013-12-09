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

package juzu.impl.compiler.file;

import juzu.impl.common.FileKey;
import juzu.impl.common.Resource;
import juzu.impl.common.Timestamped;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ReadWriteFileSystem;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JavaFileObjectImpl<P> implements JavaFileObject {

  /***/
  final FileKey key;

  /** . */
  final ReadFileSystem<P> fs;

  /** The file. */
  private final P file;

  /** . */
  Timestamped<Resource> content;

  /** . */
  private boolean writing;

  /** . */
  final URI uri;

  public JavaFileObjectImpl(JavaFileManager.Location location, FileKey key, ReadFileSystem<P> fs, P file) throws NullPointerException, IOException {

    //
    if (file == null) {
      throw new NullPointerException("No null file accepted for " + key);
    }

    //
    URI uri;
    try {
      uri = fs.getURL(file).toURI();
    }
    catch (URISyntaxException e) {
      throw new IOException("Could not create uri for file " + file, e);
    }

    //
    this.key = key;
    this.fs = fs;
    this.file = file;
    this.writing = false;
    this.uri = uri;
  }

  public FileKey getKey() {
    return key;
  }

  private Timestamped<Resource> assertContent() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    } else {
      if (content == null) {
        content = fs.getResource(file);
        if (content == null) {
          throw new FileNotFoundException("File " + key + " cannot be found");
        }
      }
      return content;
    }
  }

  public File getFile() throws IOException {
    return fs.getFile(file);
  }

  public boolean isNameCompatible(String simpleName, Kind kind) {
    String baseName = simpleName + kind.extension;
    return kind.equals(getKind()) && (baseName.equals(toUri().getPath()) || toUri().getPath().endsWith("/" + baseName));
  }

  public Kind getKind() {
    return key.getKind();
  }

  public NestingKind getNestingKind() {
    return null;
  }

  public Modifier getAccessLevel() {
    return null;
  }

  public URI toUri() {
    return uri;
  }

  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    CharSequence charContent = getCharContent(ignoreEncodingErrors);
    if (charContent == null) {
      throw new IOException("No content");
    }
    return new StringReader(charContent.toString());
  }

  public boolean delete() {
    if (fs instanceof ReadWriteFileSystem) {
      try {
        return ((ReadWriteFileSystem<P>)fs).removePath(file);
      }
      catch (IOException ignore) {
        return false;
      }
    } else {
      return false;
    }
  }

  public String getName() {
    File f = fs.getFile(file);
    if (f != null) {
      try {
        return f.getCanonicalPath();
      }
      catch (IOException ignore) {
      }
    }
    return key.rawName;
  }

  /**
   * We subclass this in order to have the correct name in the stack trace. This is not really documented anywhere.
   *
   * @return the file name
   */
  @Override
  public String toString() {
    return key.rawName;
  }

  public long getLastModified() {
    try {
      return assertContent().getTime();
    }
    catch (IOException e) {
      // We return 0 as the javadoc say to do
      return 0;
    }
  }

  public final CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return assertContent().getObject().getCharSequence();
  }

  public final InputStream openInputStream() throws IOException {
    return assertContent().getObject().getInputStream();
  }

  public OutputStream openOutputStream() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    }
    if (fs instanceof ReadWriteFileSystem<?>) {
      final ReadWriteFileSystem<P> fs = (ReadWriteFileSystem<P>)this.fs;
      return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
          Resource content = new Resource(toByteArray(), null);
          long lastModified = fs.updateResource(file, content);
          JavaFileObjectImpl.this.content = new Timestamped<Resource>(lastModified, content);
          JavaFileObjectImpl.this.writing = false;
        }
      };
    }
    else {
      throw new UnsupportedOperationException("Read only");
    }
  }

  public Writer openWriter() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    }
    if (fs instanceof ReadWriteFileSystem<?>) {
      final ReadWriteFileSystem<P> fs = (ReadWriteFileSystem<P>)this.fs;
      return new StringWriter() {
        @Override
        public void close() throws IOException {
          Resource content = new Resource(getBuffer());
          long lastModified = fs.updateResource(file, content);
          JavaFileObjectImpl.this.content = new Timestamped<Resource>(lastModified, content);
          JavaFileObjectImpl.this.writing = false;
        }
      };
    }
    else {
      throw new UnsupportedOperationException("Read only");
    }
  }
}
