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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation declares a java type to be a valid controller method parameter type:
 * <p/>
 * <code><pre>
 *    public class Controller {
 *       &#064;{@link View} public {@link Response.Content} display(User user) { ...}
 *    }
 * <p/>
 *    &#064;{@link Mapped}
 *    public class User {
 *       ...
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Mapped {
}
