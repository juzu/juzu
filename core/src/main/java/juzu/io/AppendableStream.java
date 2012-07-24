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

package juzu.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;


/**
 * Implementation of the {@link juzu.io.Stream.Char} interface that uses an appendable delegate.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AppendableStream implements Stream.Char {

  /** . */
  protected final Appendable delegate;

  public AppendableStream(Appendable delegate) {
    if (delegate == null) {
      throw new NullPointerException("No null writer accepted");
    }

    //
    this.delegate = delegate;
  }

  public Stream.Char append(char c) throws IOException {
    delegate.append(c);
    return this;
  }

  public Stream.Char append(CharSequence s) throws IOException {
    delegate.append(s);
    return this;
  }

  public Stream.Char append(CharSequence csq, int start, int end) throws IOException {
    delegate.append(csq, start, end);
    return this;
  }

  public Stream.Char append(CharArray chars) throws IOException {
    chars.write(delegate);
    return this;
  }

  public void close() throws IOException {
    if (delegate instanceof Closeable) {
      ((Closeable)delegate).close();
    }
  }

  public void flush() throws IOException {
    if (delegate instanceof Flushable) {
      ((Flushable)delegate).flush();
    }
  }
}
