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

import juzu.impl.common.MimeType;
import juzu.impl.common.PercentCodec;

import java.io.IOException;

/**
 * An uri writer.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class URIWriter {

  /** . */
  private MimeType mimeType;

  /** . */
  private Appendable appendable;

  /** . */
  private boolean questionMarkDone;

  /**
   * Create a new URI writer.
   *
   * @param appendable the appendable
   * @param mimeType   the mime type
   * @throws NullPointerException if the appendable argument is null
   */
  public URIWriter(Appendable appendable, MimeType mimeType) throws NullPointerException {
    if (appendable == null) {
      throw new NullPointerException("No null appendable accepted");
    }

    //
    this.appendable = appendable;
    this.mimeType = mimeType;
  }

  /**
   * Create a new URI writer.
   *
   * @param appendable the appendable
   * @throws NullPointerException if the appendable argument is null
   */
  public URIWriter(Appendable appendable) throws NullPointerException {
    this(appendable, null);
  }

  public MimeType getMimeType() {
    return mimeType;
  }

  public void setMimeType(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void append(char c) throws IOException {
    appendable.append(c);
  }

  public void append(String s) throws IOException {
    appendable.append(s);
  }

  /**
   * Append a segment to the path.
   *
   * @param c the char to append
   * @throws IllegalStateException if a query parameter was already appended
   * @throws IOException           any IO exception
   */
  public void appendSegment(char c) throws IllegalStateException, IOException {
    if (questionMarkDone) {
      throw new IllegalStateException("Query separator already written");
    }
    PercentCodec.PATH_SEGMENT.encode(c, appendable);
  }

  /**
   * Append a segment to the path.
   *
   * @param s the string to append.
   * @throws NullPointerException  if any argument value is null
   * @throws IllegalStateException if a query parameter was already appended
   * @throws IOException           any IO exception
   */
  public void appendSegment(String s) throws NullPointerException, IllegalStateException, IOException {
    if (s == null) {
      throw new NullPointerException("No null path accepted");
    }
    for (int len = s.length(), i = 0;i < len;i++) {
      char c = s.charAt(i);
      appendSegment(c);
    }
  }

  /**
   * Append a query parameter to the parameter set. Note that the query parameters are ordered and the sequence of call
   * to this method should be honoured when an URL is generated. Note also that the same parameter name can be used
   * multiple times.
   *
   * @param parameterName  the parameter name
   * @param paramaterValue the parameter value
   * @throws NullPointerException if any argument value is null
   * @throws IOException          any IOException
   */
  public void appendQueryParameter(String parameterName, String paramaterValue) throws NullPointerException, IOException {
    if (parameterName == null) {
      throw new NullPointerException("No null parameter name accepted");
    }
    if (paramaterValue == null) {
      throw new NullPointerException("No null parameter value accepted");
    }

    //
    MimeType mt = mimeType;
    if (mt == null) {
      mt = MimeType.XHTML;
    }

    //
    appendable.append(questionMarkDone ? mt.amp : "?");
    PercentCodec.QUERY_PARAM.encode(parameterName, appendable);
    appendable.append('=');
    PercentCodec.QUERY_PARAM.encode(paramaterValue, appendable);
    questionMarkDone = true;
  }

  /**
   * Reset the writer for reuse.
   *
   * @param appendable the used appendable
   */
  public void reset(Appendable appendable) {
    this.appendable = appendable;
    this.questionMarkDone = false;
  }
}
