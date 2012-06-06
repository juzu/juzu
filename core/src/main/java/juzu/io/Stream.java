package juzu.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface Stream extends Flushable, Closeable {
  /** A stream that extends the appendable interface and add support for the {@link juzu.io.CharArray} class. */
  interface Char extends Stream, Appendable {

    Char append(CharArray chars) throws IOException;

    Char append(CharSequence csq) throws IOException;

    Char append(CharSequence csq, int start, int end) throws IOException;

    Char append(char c) throws IOException;

  }

  /** A binary stream. */
  interface Binary extends Stream {

    Binary append(byte[] data) throws IOException;

    Binary append(byte[] data, int off, int len) throws IOException;

  }
}
