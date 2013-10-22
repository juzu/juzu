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

import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class JUL extends juzu.impl.common.Logger {

  /** . */
  private static final ConcurrentHashMap<String, JUL> loggers = new ConcurrentHashMap<String, JUL>();

  public static juzu.impl.common.Logger getLogger(String name) {
    JUL logger = loggers.get(name);
    if (logger == null) {
      Logger delegate = Logger.getLogger(name);
      loggers.put(name, logger = new JUL(delegate));
    }
    return logger;
  }

  /** . */
  private final Logger delegate;

  private JUL(Logger delegate) {
    this.delegate = delegate;
  }

  @Override
  protected void send(Level level, CharSequence msg, Throwable t) {
    if (t != null) {
      delegate.log(level, msg.toString(), t);
    } else {
      delegate.log(level, msg.toString());
    }
  }
}
