/*
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
package juzu.impl.asset;

import juzu.impl.common.CompositeReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** @author Julien Viet */
public class NormalizeJSReader extends Reader {

  private boolean finished = false;

  private boolean multiComments = false;

  private boolean singleComment = false;

  private Reader sub;

  public NormalizeJSReader(Reader sub) {
    this.sub = sub;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (finished) {
      return sub.read(cbuf, off, len);
    } else {
      char[] buffer = new char[len];
      int relLen = sub.read(buffer, 0, len);
      if (relLen == -1) {
        finished = true;
        return -1;
      } else {
        int r = off;

        for (int i = 0; i < relLen; i++) {
          char c = buffer[i];

          char next = 0;
          boolean skip = false, overflow = (i + 1 == relLen);
          if (!finished) {
            skip = true;
            if (!singleComment && c == '/' && (next = readNext(buffer, i, overflow)) == '*') {
              multiComments = true;
              i++;
            } else if (!singleComment && c == '*' && (next = readNext(buffer, i, overflow)) == '/') {
              multiComments = false;
              i++;
            } else if (!multiComments && c == '/' && next == '/') {
              singleComment = true;
              i++;
            } else if (c == '\n') {
              singleComment = false;
            } else if (c != ' ') {
              skip = false;
            }

            if (!skip && !multiComments && !singleComment) {
              if (next != 0 && overflow) {
                sub = new CompositeReader(new StringReader(String.valueOf(c)), sub);
              }
              cbuf[r++] = c;
              finished = true;
            }
          } else {
            cbuf[r++] = c;
          }
        }
        return r - off;
      }
    }
  }

  private char readNext(char[] buffer, int i, boolean overflow) throws IOException {
    char c = 0;
    if (overflow) {
      int tmp = sub.read();
      if (tmp != -1) {
        c = (char)tmp;
      }
    } else {
      c = buffer[i + 1];
    }
    return c;
  }

  @Override
  public void close() throws IOException {
    sub.close();
  }
}
