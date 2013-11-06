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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** @author Julien Viet */
public abstract class Completion<T> {

  public abstract boolean isFailed();

  public abstract T get();

  public abstract Exception getCause();

  public static <T> Completion<T> future(Callable<T> callable) {
    FutureTask<T> task = new FutureTask<T>(callable);
    return future(task, task);
  }

  public static <T> Completion<T> future(Future<T> future) {
    return future(future, null);
  }

  private static <T> Completion<T> future(final Future<T> future, final Runnable runnable) {
    return new Completion<T>() {

      @Override
      public boolean isFailed() {
        if (runnable != null) {
          runnable.run();
        }
        try {
          future.get();
          return false;
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError("should not be here");
        }
        catch (ExecutionException e) {
          return true;
        }
      }

      @Override
      public T get() {
        if (runnable != null) {
          runnable.run();
        }
        try {
          return future.get();
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError("should not be here");
        }
        catch (ExecutionException e) {
          return null;
        }
      }

      @Override
      public Exception getCause() {
        if (runnable != null) {
          runnable.run();
        }
        try {
          future.get();
          return null;
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError("should not be here");
        }
        catch (ExecutionException e) {
          Throwable cause = e.getCause();
          return cause instanceof Exception ? (Exception)cause : new UndeclaredThrowableException(cause);
        }
      }
    };
  }

  public static <T> Completion<T> completed(final T value) {
    return new Completion<T>() {
      @Override
      public T get() {
        return value;
      }
      @Override
      public boolean isFailed() {
        return false;
      }
      @Override
      public Exception getCause() {
        return null;
      }
    };
  }

  public static <T> Completion<T> failed(final Exception cause) {
    return new Completion<T>() {
      @Override
      public boolean isFailed() {
        return true;
      }
      @Override
      public T get() {
        return null;
      }
      @Override
      public Exception getCause() {
        return cause;
      }
    };
  }
}
