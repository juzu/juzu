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
 * A basic logger for now.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface Logger {

  Logger SYSTEM = new Logger() {
    public void log(CharSequence msg) {
      System.out.println(msg);
    }
    public void log(CharSequence msg, Throwable t) {
      System.err.println(msg);
      t.printStackTrace();
    }
  };

  /**
   * Log a message.
   *
   * @param msg the message
   */
  void log(CharSequence msg);

  /**
   * Log a message assocated with a throwable.
   *
   * @param msg the message
   * @param t   the throwable
   */
  void log(CharSequence msg, Throwable t);

}
