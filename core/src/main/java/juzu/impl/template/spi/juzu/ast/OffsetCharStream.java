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

package juzu.impl.template.spi.juzu.ast;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetCharStream extends SimpleCharStream {

  /** . */
  public int beginOffset;

  /** . */
  public int currentOffset;

  public OffsetCharStream(OffsetReader r) {
    super(r);
  }

  public char BeginToken() throws IOException {
    char c = super.BeginToken();
    beginOffset = currentOffset;
    return c;
  }

  public char readChar() throws IOException {
    char c = super.readChar();
    currentOffset++;
    return c;
  }

  public void backup(int amount) {
    super.backup(amount);
    currentOffset -= amount;
  }

  public CharSequence getData() {
    return ((OffsetReader)inputStream).getData();
  }
}
