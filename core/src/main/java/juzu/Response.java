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

import juzu.io.Streamable;
import juzu.io.Streams;
import juzu.request.Dispatch;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A response object signalling to the portal the action to take after an interaction. This object is usually
 * returned after the invocation of a controller method and instructs Juzu the action to take.</p>
 *
 * <h2>Action response</h2>
 *
 * <h3>Redirection response</h3>
 * <p>A <code>Response.Process.Action.Redirect</code> response instructs Juzu to make a redirection to a valid
 * URL after the interaction, this kind of response is created using the factory method {@link Response#redirect(String)}:
 * <code><pre>
 *    return Response.redirect("http://www.exoplatform.org");
 * </pre></code>
 * </p>
 *
 * <h3>Proceed to render phase</h3>
 * <p>A <code>Response.View</code> response instructs Juzu to proceed to the render phase of a valid view
 * controller, this kind of response can be created using an {@link juzu.request.ActionContext}, however the best
 * way is to use a controller companion class that carries method factories for creating render responses.</p>
 *
 * <p>Type safe {@link juzu.Response.View} factory method are generated for each view or resource controller
 * methods. The signature of an render factory is obtained by using the same signature of the controller method.</p> <p/>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Action
 *       public {@link juzu.Response.View} myAction() {
 *          return MyController_.myRender("hello");
 *       }
 *
 *       &#064;View
 *       public void myRender(String param) {
 *       }
 *    }
 * </pre></code>
 *
 * <h2>Mime response</h2>
 *
 * <p>Mime response are used by the {@link juzu.request.Phase#VIEW} and the {@link juzu.request.Phase#RESOURCE} phases.
 * Both contains a content to be streamed to the client but still they have some noticeable differences.<p/>
 *
 * <p>The {@link juzu.Response.Content} class is the base response class which will work well for the two phases.
 * However the {@link juzu.request.Phase#VIEW} can specify an optional title and the {@link juzu.request.Phase#RESOURCE}
 * can specify an optional status code for the user agent response.</p>
 *
 * <p>Responses are created using the {@link Response} factory methods such as</p>
 *
 * <ul>
 *   <li>{@link Response#ok} creates an ok response</li>
 *   <li>{@link Response#notFound} creates a not found response</li>
 * </ul>
 *
 * <p>Response can also created from {@link juzu.template.Template} directly:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") {@link juzu.template.Template} index;
 *
 *       &#064;View
 *       public {@link juzu.Response.Content} myView() {
 *          return index.render();
 *       }
 *
 *       &#064;Inject &#064;Path("error.gtmpl")  {@link juzu.template.Template} error;
 *
 *       &#064;Resource
 *       public {@link juzu.Response.Content} myView() {
 *          return error.notFound();
 *       }
 *    }
 * </pre></code>
 *
 * <p>The {@link juzu.template.Template.Builder} can also create responses:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") index index;
 *
 *       &#064;View
 *       public {@link juzu.Response.Content} myView() {
 *          return index.with().label("hello").render();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Response {

  /** . */
  protected final PropertyMap properties;

  protected Response() {
    this.properties = new PropertyMap();
  }

  protected Response(PropertyMap properties) {
    this.properties = properties;
  }

  /**
   * Set a property, if the value is null, the property is removed.
   *
   * @param propertyType the property type
   * @param propertyValue the property value
   * @throws NullPointerException if the property type is null
   */
  public <T> Response with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
    if (propertyType == null) {
      throw new NullPointerException("No null property type allowed");
    }
    properties.setValue(propertyType, propertyValue);
    return this;
  }

  /**
   * Removes a property.
   *
   * @param propertyType the property type
   * @throws NullPointerException if the property type is null
   */
  public <T> Response without(PropertyType<T> propertyType) throws NullPointerException {
    return with(propertyType, null);
  }

  /**
   * Set a boolean property to true.
   *
   * @param propertyType the property type
   * @throws NullPointerException if the property type is null
   */
  public Response with(PropertyType<Boolean> propertyType) throws NullPointerException {
    return with(propertyType, true);
  }

  /**
   * Set a boolean property to false.
   *
   * @param propertyType the property type
   * @throws NullPointerException if the property type is null
   */
  public Response withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
    return with(propertyType, false);
  }

  public final PropertyMap getProperties() {
    return properties;
  }

  public Response withHeader(String name, String... value) {
    Iterable<Map.Entry<String, String[]>> values = properties.getValues(PropertyType.HEADER);
    if (values != null) {
      for (Map.Entry<String, String[]> header : values) {
        if (header.getKey().equals(name)) {
          header.setValue(value);
          return this;
        }
      }
    }
    properties.addValue(PropertyType.HEADER, new AbstractMap.SimpleEntry<String, String[]>(name, value));
    return this;
  }

  public static abstract class Action extends Response {

    public abstract Map<String, String[]> getParameters();

  }

  /**
   * A response instructing to execute a render phase of a controller method after the current interaction.
   */
  public static abstract class View extends Response implements Dispatch {

    @Override
    public <T> View with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (View)super.with(propertyType, propertyValue);
    }

    @Override
    public View withHeader(String name, String... value) {
      return (View)super.withHeader(name, value);
    }

    @Override
    public <T> View without(PropertyType<T> propertyType) throws NullPointerException {
      return (View)super.without(propertyType);
    }

    @Override
    public View with(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (View)super.with(propertyType);
    }

    @Override
    public View withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (View)super.withNo(propertyType);
    }

    public abstract boolean equals(Object obj);

  }

  /**
   * A response instructing to execute an HTTP redirection after the current interaction.
   */
  public static class Redirect extends Response {

    /** . */
    private final String location;

    public Redirect(String location) {
      this.location = location;
    }

    public String getLocation() {
      return location;
    }

    @Override
    public <T> Redirect with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Redirect)super.with(propertyType, propertyValue);
    }

    @Override
    public Redirect withHeader(String name, String... value) {
      return (Redirect)super.withHeader(name, value);
    }

    @Override
    public <T> Redirect without(PropertyType<T> propertyType) throws NullPointerException {
      return (Redirect)super.without(propertyType);
    }

    @Override
    public Redirect with(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Redirect)super.with(propertyType);
    }

    @Override
    public Redirect withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Redirect)super.withNo(propertyType);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Redirect) {
        Redirect that = (Redirect)obj;
        return location.equals(that.location);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Response.Redirect[location" + location + "]";
    }
  }
  
  public static class Status extends Response {

    /** . */
    private int code;

    public Status(int code) {
      this.code = code;
    }

    public Status(int code, PropertyMap properties) {
      super(properties);
      
      //
      this.code = code;
    }

    public final int getCode() {
      return code;
    }
    
    public Body body(Streamable s) {
      return new Body(code, properties, s);
    }

    public Body body(CharSequence s) {
      return body(new Streamable.CharSequence(s));
    }

    public Body body(byte[] s) {
      return body(new Streamable.Bytes(s));
    }

    public Body body(InputStream s) {
      return body(new Streamable.InputStream(s));
    }

    public Content content(Streamable s) {
      return new Content(code, properties, s);
    }

    public Content content(CharSequence s) {
      return content(new Streamable.CharSequence(s));
    }

    public Content content(byte[] s) {
      return content(new Streamable.Bytes(s));
    }

    public Content content(InputStream s) {
      return content(new Streamable.InputStream(s));
    }
  }

  public static class Body extends Status {

    /** . */
    private Streamable streamable;

    protected Body(int status, PropertyMap properties) {
      super(status, properties);

      //
      this.streamable = null;
    }

    protected Body(int status, Streamable streamable) {
      super(status);
      
      //
      this.streamable = streamable;
    }

    protected Body(int status, PropertyMap properties, Streamable streamable) {
      super(status, properties);

      //
      this.streamable = streamable;
    }

    public Streamable getStreamable() {
      return streamable;
    }

    public String getMimeType() {
      return properties.getValue(PropertyType.MIME_TYPE);
    }

    public Charset getCharset() {
      return properties.getValue(PropertyType.CHARSET);
    }

    public Body withCharset(Charset charset) {
      properties.setValue(PropertyType.CHARSET, charset);
      return this;
    }

    public Body withMimeType(String mimeType) {
      properties.setValue(PropertyType.MIME_TYPE, mimeType);
      return this;
    }

    @Override
    public Body withHeader(String name, String... value) {
      return (Body)super.withHeader(name, value);
    }

    @Override
    public <T> Body with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Body)super.with(propertyType, propertyValue);
    }

    @Override
    public <T> Body without(PropertyType<T> propertyType) throws NullPointerException {
      return (Body)super.without(propertyType);
    }

    @Override
    public Body with(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Body)super.with(propertyType);
    }

    @Override
    public Body withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Body)super.withNo(propertyType);
    }
  }

  public static class Content extends Body {

    public Content(int status, PropertyMap properties, Streamable streamable) {
      super(status, properties, streamable);
    }

    public Content(int status, Streamable streamable) {
      super(status, streamable);
    }

    public Content(PropertyMap properties, Streamable streamable) {
      super(200, properties, streamable);
    }

    public Content(Streamable streamable) {
      super(200, streamable);
    }

    @Override
    public <T> Content with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Content)super.with(propertyType, propertyValue);
    }

    @Override
    public <T> Content without(PropertyType<T> propertyType) throws NullPointerException {
      return (Content)super.without(propertyType);
    }

    @Override
    public Content with(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Content)super.with(propertyType);
    }

    @Override
    public Content withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Content)super.withNo(propertyType);
    }

    @Override
    public Content withMimeType(String mimeType) {
      return (Content)super.withMimeType(mimeType);
    }

    @Override
    public Content withCharset(Charset charset) {
      return (Content)super.withCharset(charset);
    }

    @Override
    public Content withHeader(String name, String... value) {
      return (Content)super.withHeader(name, value);
    }

    public String getTitle() {
      return properties.getValue(PropertyType.TITLE);
    }

    public Content withTitle(String title) {
      properties.setValue(PropertyType.TITLE, title);
      return this;
    }

    public Content withScripts(String... scripts) throws NullPointerException {
      if (scripts == null) {
        throw new NullPointerException("No null script accepted");
      }
      properties.addValues(PropertyType.SCRIPT, scripts);
      return this;
    }

    public Content withStylesheets(String... stylesheets) throws NullPointerException {
      if (stylesheets == null) {
        throw new NullPointerException("No null stylesheet accepted");
      }
      properties.addValues(PropertyType.STYLESHEET, stylesheets);
      return this;
    }

    public Content withMetaTag(String name, String value) {
      Iterable<Map.Entry<String, String>> values = properties.getValues(PropertyType.META_TAG);
      if (values != null) {
        for (Map.Entry<String, String> meta : values) {
          if (meta.getKey().equals(name)) {
            meta.setValue(value);
            return this;
          }
        }
      }
      properties.addValue(PropertyType.META_TAG, new AbstractMap.SimpleEntry<String, String>(name, value));
      return this;
    }

    @Override
    public String toString() {
      return "Response.Content[]";
    }
  }

  public static class Error extends Response {

    /** . */
    private final List<StackTraceElement> at;

    /** . */
    private final Throwable cause;

    /** . */
    private final String msg;

    public Error(Throwable cause) {
      this(null, cause);
    }

    public Error(String message) {
      this(message, null);
    }

    private Error(String message, Throwable cause) {
      this.at = Collections.unmodifiableList(Arrays.asList(new Exception().getStackTrace()));
      this.cause = cause;
      this.msg = message;
    }

    public List<StackTraceElement> getAt() {
      return at;
    }

    public Throwable getCause() {
      return cause;
    }

    public String getMessage() {
      return msg;
    }

    @Override
    public String toString() {
      return "Response.Error[" + (cause != null ? cause.getMessage() : "") + "]";
    }
  }

  public static Response.Redirect redirect(String location) {
    return new Response.Redirect(location);
  }

  public static Status status(int code) {
    return new Status(code);
  }

  public static Status ok() {
    return status(200);
  }

  public static Status notFound() {
    return status(404);
  }

  public static Content ok(InputStream content) {
    return content(200, content);
  }

  public static Content ok(byte[] content) {
    return content(200, content);
  }

  public static Content ok(Readable content) {
    return content(200, content);
  }

  public static Content ok(CharSequence content) {
    return content(200, content);
  }

  public static Content notFound(byte[] content) {
    return content(404, content);
  }

  public static Content notFound(InputStream content) {
    return content(404, content);
  }

  public static Content notFound(Readable content) {
    return content(404, content);
  }

  public static Content notFound(CharSequence content) {
    return content(404, content);
  }

  public static Content content(int code, byte[] content) {
    return content(code, new Streamable.Bytes(content));
  }

  public static Content content(int code, InputStream content) {
    return content(code, new Streamable.InputStream(content));
  }

  public static Content content(int code, Readable content) {
    return content(code, Streams.streamable(content));
  }

  public static Content content(int code, CharSequence content) {
    return content(code, new Streamable.CharSequence(content));
  }

  public static Content content(int code, Streamable content) {
    return new Content(code, content);
  }

  public static Error error(Throwable t) {
    return new Error(t);
  }

  public static Error error(String msg) {
    return new Error(msg);
  }
}
