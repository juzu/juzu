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

import java.util.logging.Level;

/**
 * The logger.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Logger {

  /**
   * Logger that send to {@link System#out} and {@link System#err}.
   */
  public static final Logger SYSTEM = new Logger() {
    @Override
    protected void send(Level level, CharSequence msg, Throwable t) {
      if (t != null) {
        System.err.println("[" + level.getName() + "] " + msg);
      } else {
        System.out.println("[" + level.getName() + "] " + msg);
      }
    }
  };

  /** A formatting string that can be helpful for implementations, Expected arguments are
   * 1: date
   * 2: level
   * 3: name
   * 4: message
   */
  public static final String FORMAT = "%1$tH:%1$tM,%1$tS:%1$tL %2$-7s [%3$s]: %4$s";

  /**
   * Send a message to the logger, the throwable can be null.
   *
   * @param msg the message
   * @param t   the optional throwable
   */
  protected void send(Level level, CharSequence msg, Throwable t) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Log a message.
   *
   * @param msg the message
   */
  public void log(Level level, CharSequence msg) {
    log(level, msg, null);
  }

  /**
   * Log a message assocated with a throwable.
   *
   * @param msg the message
   * @param t   the throwable
   */
  public void log(Level level, CharSequence msg, Throwable t) {
    switch (level.intValue()) {
      case 1000: // SEVERE
        error(msg, t);
        break;
      case 900: // WARNING
        warning(msg, t);
        break;
      case 800: // INFO
        info(msg, t);
        break;
      case 700: // CONFIG
        error(msg, t);
        break;
      case 500: // FINE
        debug(msg, t);
        break;
      case 400: // FINER
        debug(msg, t);
        break;
      case 300: // FINEST
        trace(msg, t);
        break;
    }
  }

  public void error(CharSequence msg) {
    send(Level.SEVERE, msg, null);
  }

  public void error(CharSequence msg, Throwable t) {
    send(Level.SEVERE, msg, t);
  }

  public void warning(CharSequence msg) {
    send(Level.WARNING, msg, null);
  }

  public void warning(CharSequence msg, Throwable t) {
    send(Level.WARNING, msg, t);
  }

  public void info(CharSequence msg) {
    send(Level.INFO, msg, null);
  }

  public void info(CharSequence msg, Throwable t) {
    send(Level.INFO, msg, t);
  }

  public void debug(CharSequence msg) {
    send(Level.FINER, msg, null);
  }

  public void debug(CharSequence msg, Throwable t) {
    send(Level.FINER, msg, t);
  }

  public void trace(CharSequence msg) {
    send(Level.FINEST, msg, null);
  }

  public void trace(CharSequence msg, Throwable t) {
    send(Level.FINEST, msg, t);
  }
}
