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

package juzu.impl.fs;

import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Content;
import juzu.impl.common.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class FileSystemScanner<P> implements Filter<P> {

  public static <P> FileSystemScanner<P> createTimestamped(ReadFileSystem<P> fs) {
    return new Timestamped<P>(fs);
  }

  public static <P> FileSystemScanner<P> createHashing(ReadFileSystem<P> fs) {
    return new Hash<P>(fs);
  }

  public static class Timestamped<P> extends FileSystemScanner<P> {
    public Timestamped(ReadFileSystem<P> fs) {
      super(fs);
    }

    @Override
    protected long stampOf(P file) throws IOException {
      return fs.getLastModified(file);
    }

    @Override
    protected boolean isModified(long snapshot, long current) {
      return snapshot < current;
    }
  }

  public static class Hash<P> extends FileSystemScanner<P> {
    public Hash(ReadFileSystem<P> fs) {
      super(fs);
    }

    @Override
    protected long stampOf(P file) throws IOException {
      try {
        juzu.impl.common.Timestamped<Content> content = fs.getContent(file);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream in = content.getObject().getInputStream();
        byte[] bytes = Tools.bytes(in);
        byte[] md5 = digest.digest(bytes);
        long value = 0;
        for (byte b : md5) {
          value = value * 256 + Tools.unsignedByteToInt(b);
        }
        return value;
      }
      catch (NoSuchAlgorithmException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    protected boolean isModified(long snapshot, long current) {
      return snapshot != current;
    }
  }

  /** . */
  protected final ReadFileSystem<P> fs;

  /** . */
  final ArrayList<String> stack = new ArrayList<String>();

  private FileSystemScanner(ReadFileSystem<P> fs) {
    this.fs = fs;
  }

  public ReadFileSystem<P> getFileSystem() {
    return fs;
  }

  public Snapshot<P> take() {
    return new Snapshot<P>(this);
  }

  public boolean acceptDir(P dir, String name) throws IOException {
    return !name.startsWith(".");
  }

  public boolean acceptFile(P file, String name) throws IOException {
    return !name.startsWith(".");
  }

  protected abstract long stampOf(P file) throws IOException;

  protected abstract boolean isModified(long snapshot, long current);

}
