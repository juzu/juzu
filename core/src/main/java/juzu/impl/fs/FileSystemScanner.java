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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class FileSystemScanner<P> implements Visitor<P>, Filter<P> {

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
    protected long createValue(P file) throws IOException {
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
    protected long createValue(P file) throws IOException {
      try {
        Content content = fs.getContent(file);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream in = content.getInputStream();
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
  private StringBuilder sb = new StringBuilder();

  /** . */
  private Map<String, Data> snapshot;

  private FileSystemScanner(ReadFileSystem<P> fs) {
    this.snapshot = new HashMap<String, Data>();
    this.fs = fs;
  }

  private static class Data {

    /** . */
    private long lastModified;

    /** . */
    private Change change;

    private Data(long lastModified) {
      this.lastModified = lastModified;
      this.change = Change.ADD;
    }
  }

  public ReadFileSystem<P> getFileSystem() {
    return fs;
  }

  public Map<String, Change> scan() throws IOException {
    // Mark everything as removed
    for (Data data : snapshot.values()) {
      data.change = Change.REMOVE;
    }

    // Update map
    fs.traverse(this, this);

    // Cleanup map and build change map
    Map<String, Change> changes = new LinkedHashMap<String, Change>();
    for (Iterator<Map.Entry<String, Data>> i = snapshot.entrySet().iterator();i.hasNext();) {
      Map.Entry<String, Data> entry = i.next();
      Data data = entry.getValue();
      if (data.change != null) {
        changes.put(entry.getKey(), data.change);
        if (data.change == Change.REMOVE) {
          i.remove();
        }
      }
    }

    //
    return changes;
  }

  public boolean acceptDir(P dir, String name) throws IOException {
    return !name.startsWith(".");
  }

  public boolean acceptFile(P file, String name) throws IOException {
    return !name.startsWith(".");
  }

  public void enterDir(P dir, String name) throws IOException {
  }

  public void file(P file, String name) throws IOException {
    long lastModified = createValue(file);
    fs.pathOf(file, '/', sb);
    String id = sb.toString();
    sb.setLength(0);
    Data data = snapshot.get(id);
    if (data == null) {
      snapshot.put(id, new Data(lastModified));
    }
    else {
      if (isModified(data.lastModified, lastModified)) {
        data.lastModified = lastModified;
        data.change = Change.UPDATE;
      }
      else {
        data.change = null;
      }
    }
  }

  public void leaveDir(P dir, String name) throws IOException {
  }

  protected abstract long createValue(P file) throws IOException;

  protected abstract boolean isModified(long snapshot, long current);
}
