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

package juzu.impl.compiler.file;

import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.common.Content;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JavaFileObjectImpl<P> extends SimpleJavaFileObject {

  /***/
  final FileKey key;

  /** . */
  final SimpleFileManager<P> manager;

  /** The file, might not exist when this object will create. */
  private P file;

  /** . */
  Content content;

  /** . */
  private boolean writing;

  public JavaFileObjectImpl(FileKey key, SimpleFileManager<P> manager, P file) {
    super(key.uri, key.kind);

    //
    this.key = key;
    this.manager = manager;
    this.file = file;
    this.writing = false;
  }

  public FileKey getKey() {
    return key;
  }

  private Content getContent() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    }
    if (file == null) {
      throw new FileNotFoundException("File " + key + " cannot be found");
    }
    if (content == null) {
      content = manager.fs.getContent(file);
    }
    return content;
  }

  public File getFile() throws IOException {
    return manager.fs.getFile(file);
  }

  @Override
  public String getName() {
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

  @Override
  public long getLastModified() {
    try {
      return getContent().getLastModified();
    }
    catch (IOException e) {
      // We return 0 as the javadoc say to do
      return 0;
    }
  }

  @Override
  public final CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return getContent().getCharSequence();
  }

  @Override
  public final InputStream openInputStream() throws IOException {
    return getContent().getInputStream();
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    }
    if (manager.fs instanceof ReadWriteFileSystem<?>) {
      final ReadWriteFileSystem<P> fs = (ReadWriteFileSystem<P>)manager.fs;
      return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
          content = new Content(System.currentTimeMillis(), toByteArray(), null);
          P file = fs.makeFile(key.packageNames, key.name);
          fs.setContent(file, content);
          JavaFileObjectImpl.this.file = file;
          JavaFileObjectImpl.this.writing = false;
        }
      };
    }
    else {
      throw new UnsupportedOperationException("Read only");
    }
  }

  @Override
  public Writer openWriter() throws IOException {
    if (writing) {
      throw new IllegalStateException("Opened for writing");
    }
    if (manager.fs instanceof ReadWriteFileSystem<?>) {
      final ReadWriteFileSystem<P> fs = (ReadWriteFileSystem<P>)manager.fs;
      return new StringWriter() {
        @Override
        public void close() throws IOException {
          content = new Content(System.currentTimeMillis(), getBuffer());
          P file = fs.makeFile(key.packageNames, key.name);
          fs.setContent(file, content);
          JavaFileObjectImpl.this.file = file;
          JavaFileObjectImpl.this.writing = false;
        }
      };
    }
    else {
      throw new UnsupportedOperationException("Read only");
    }
  }
}
