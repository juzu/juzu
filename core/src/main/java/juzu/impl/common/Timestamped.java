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

/**
 * A wrapped for decorate an object with a timestamp, this object is not modifable.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class Timestamped<T> {

  /** . */
  private final long time;

  /** . */
  private final T object;

  public Timestamped(long time, T object) {
    this.time = time;
    this.object = object;
  }

  public T getObject() {
    return object;
  }

  public long getTime() {
    return time;
  }

  public Timestamped<T> touch() {
    return new Timestamped<T>(System.currentTimeMillis(), object);
  }

  @Override
  public String toString() {
    return "Timestamped[time=" + time + ",object=" + object + "]";
  }
}
