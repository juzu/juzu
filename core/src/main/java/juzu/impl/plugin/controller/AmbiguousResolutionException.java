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

package juzu.impl.plugin.controller;

/**
 * Denoates something ambiguous that could not be resolved.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AmbiguousResolutionException extends RuntimeException {

  public AmbiguousResolutionException() {
  }

  public AmbiguousResolutionException(String message) {
    super(message);
  }

  public AmbiguousResolutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public AmbiguousResolutionException(Throwable cause) {
    super(cause);
  }
}
