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

package juzu.impl.inject.spi.guice;

import com.google.inject.spi.InjectionListener;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PostConstructInjectionListener implements InjectionListener<Object> {
  public void afterInjection(Object injectee) {
    for (Method method : injectee.getClass().getMethods()) {
      if (
        Modifier.isPublic(method.getModifiers()) &&
          !Modifier.isStatic(method.getModifiers()) &&
          method.getAnnotation(PostConstruct.class) != null) {
        try {
          method.invoke(injectee);
        }
        catch (IllegalAccessException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
        catch (InvocationTargetException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
      }
    }
  }
}
