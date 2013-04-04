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

package juzu;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * An unchecked wrapper for {@link IOException}. Note that this exception should not be abused, i.e it should not be
 * used in place of <code>IOException</code>. It should be used in the Juzu API used to develop an application, which
 * makes the <code>IOException</code> transparent to the developer, for instance we want to void the developer to care
 * about the <code>IOException</code> in this situation:
 * <p/>
 * <pre><code>
 *    public void index() throws IOException {
 *       template.render();
 *    }
 * </pre></code>
 * <p/>
 * Instead the {@link juzu.template.Template#render()} methods wraps the <code>IOException</code> with this unchecked
 * wrapper and the developer does not have to care about it anymore:
 * <p/>
 * <pre><code>
 *    public void index() {
 *       template.render();
 *    }
 * </code></pre>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class UndeclaredIOException extends UndeclaredThrowableException {

  public UndeclaredIOException(IOException undeclaredIO) {
    super(undeclaredIO);
  }

  @Override
  public IOException getCause() {
    return (IOException)super.getCause();
  }
}
