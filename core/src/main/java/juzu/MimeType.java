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
 * <p>Associate a program element with a mime type.</p>
 *
 * <p>Java controller methods can be annotated with this annotation to associate the controller response
 * with a mime type automatically:</p>
 *
 * <code><pre>
 *   &#064;MimeType("text/html")
 *   public Response index() {
 *     return Response.ok("Hello world");
 *   }
 * </pre></code>
 *
 * <p>t can also transform an existing annotation into a specialized mime type:</p>
 *
 * <code><pre>
 *   &#064;Retention(RetentionPolicy.RUNTIME)
 *   &#064;Target({ElementType.METHOD})
 *   &#064;MimeType("text/html")
 *   public @interface HTML {
 *   }
 * </pre></code>
 *
 *
 * <p>Such mime type can then be used to annotate controller methods:</p>
 * <code><pre>
 *   &#064;HTML()
 *   public Response index() {
 *     return Response.ok("Hello world");
 *   }
 * </pre></code>
 *
 * @author Julien Viet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface MimeType {

  /**
   * @return the declarations of matched mime types.
   */
  String[] value();

  /**
   * <p>Annotation matching the <code>text/html</code> mime type.</p>
   * <code><pre>
   *   &#064;MimeType.HTML
   *   public Response index() {
   *     return Response.ok("Hello world");
   *   }
   * </pre></code>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  @MimeType("text/html")
  public @interface HTML {}

  /**
   * <p>Annotation matching the <code>application/json</code> mime type.</p>
   * <code><pre>
   *   &#064;MimeType.JSON
   *   public Response index() {
   *     return Response.ok().body("{'say':'hello world'}");
   *   }
   * </pre></code>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  @MimeType("application/json")
  public @interface JSON {}

}
