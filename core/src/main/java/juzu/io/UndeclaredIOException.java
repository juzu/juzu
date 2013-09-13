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

package juzu.io;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * An unchecked wrapper for {@link IOException}. Note that this exception should not be abused, i.e it should not be
 * used in place of <code>IOException</code>. It should be used in the Juzu API used to develop an application, which
 * makes the <code>IOException</code> transparent to the developer.
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
