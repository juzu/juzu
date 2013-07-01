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
package juzu.impl.common;

import juzu.request.RequestParameter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** @author Julien Viet */
public abstract class AbstractParameterParser implements Iterable<RequestParameter> {

  /** . */
  private final CharSequence s;

  /** . */
  private final int from;

  /** . */
  private final int to;

  public AbstractParameterParser(CharSequence s, int from, int to) {
    this.s = s;
    this.from = from;
    this.to = to;
  }

  public Iterator<RequestParameter> iterator() {
    return new Iterator<RequestParameter>() {

      int current = from;
      RequestParameter next = null;

      private RequestParameter parse(int from, int to) {
        int pos = Tools.indexOf(s, '=', from, to);
        if (pos == -1) {
          String name = s.subSequence(from, to).toString();
          String decodeName = safeDecodeName(name);
          if (decodeName != null) {
            return RequestParameter.create(decodeName, "");
          }
        } else if (pos > 0) {
          String value = s.subSequence(pos + 1, to).toString();
          String decodedValue = safeDecodeValue(value);
          if (decodedValue != null) {
            String name = s.subSequence(from, pos).toString();
            String decodedName = safeDecodeName(name);
            if (decodedName != null) {
              return RequestParameter.create(decodedName, value, decodedValue);
            }
          }
        }
        return null;
      }

      public boolean hasNext() {
        while (next == null && current < to) {
          int pos = Tools.indexOf(s, '&', current, to);
          if (pos == 0) {
            throw new UnsupportedOperationException("todo");
          } else if (pos == -1) {
            next = parse(current, to);
            current = to;
          } else {
            next = parse(current, pos);
            current = pos + 1;
          }
        }
        return next != null;
      }

      public RequestParameter next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        RequestParameter tmp = next;
        next = null;
        return tmp;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  protected abstract String safeDecodeName(String s);

  protected abstract String safeDecodeValue(String s);
}
