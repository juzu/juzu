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

import juzu.impl.common.Tools;
import juzu.io.Streamable;
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
 *       public {@link juzu.Response.Render} myView() {
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

  public static class Content extends Response {

    /** . */
    private int status;

    /** . */
    private Streamable streamable;

    protected Content(int status) {
      this.status = status;
      this.streamable = null;
    }

    protected Content(int status, PropertyMap properties) {
      super(properties);

      //
      this.status = status;
      this.streamable = null;
    }

    protected Content(int status, Streamable streamable) {
      this.status = status;
      this.streamable = streamable;
    }

    protected Content(int status, PropertyMap properties, Streamable streamable) {
      super(properties);

      //
      this.status = status;
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

    public Content withCharset(Charset charset) {
      properties.setValue(PropertyType.CHARSET, charset);
      return this;
    }

    public Content withMimeType(String mimeType) {
      properties.setValue(PropertyType.MIME_TYPE, mimeType);
      return this;
    }

    @Override
    public Content withHeader(String name, String... value) {
      return (Content)super.withHeader(name, value);
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

    public Integer getStatus() {
      return status;
    }
  }

  public static class Render extends Content {

    @Override
    public <T> Render with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Render)super.with(propertyType, propertyValue);
    }

    @Override
    public <T> Render without(PropertyType<T> propertyType) throws NullPointerException {
      return (Render)super.without(propertyType);
    }

    @Override
    public Render with(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Render)super.with(propertyType);
    }

    @Override
    public Render withNo(PropertyType<Boolean> propertyType) throws NullPointerException {
      return (Render)super.withNo(propertyType);
    }

    @Override
    public Render withMimeType(String mimeType) {
      return (Render)super.withMimeType(mimeType);
    }

    @Override
    public Render withCharset(Charset charset) {
      return (Render)super.withCharset(charset);
    }

    public Render() {
      super(200);
    }

    public Render(int status, PropertyMap properties, Streamable streamable) {
      super(status, properties, streamable);
    }

    public Render(int status, Streamable streamable) {
      super(status, streamable);
    }

    public Render(PropertyMap properties, Streamable streamable) {
      super(200, properties, streamable);
    }

    public Render(Streamable streamable) {
      super(200, streamable);
    }

    @Override
    public Render withHeader(String name, String... value) {
      return (Render)super.withHeader(name, value);
    }

    public String getTitle() {
      return properties.getValue(PropertyType.TITLE);
    }

    public Render withTitle(String title) {
      properties.setValue(PropertyType.TITLE, title);
      return this;
    }

    public Iterable<String> getScripts() {
      Iterable<String> scripts = properties.getValues(PropertyType.SCRIPT);
      return scripts != null ? scripts : Tools.<String>emptyIterable();
    }

    public Render withScripts(String... scripts) throws NullPointerException {
      if (scripts == null) {
        throw new NullPointerException("No null script accepted");
      }
      properties.addValues(PropertyType.SCRIPT, scripts);
      return this;
    }

    public Iterable<String> getStylesheets() {
      Iterable<String> stylesheets = properties.getValues(PropertyType.STYLESHEET);
      return stylesheets != null ? stylesheets : Tools.<String>emptyIterable();
    }

    public Render withStylesheets(String... stylesheets) throws NullPointerException {
      if (stylesheets == null) {
        throw new NullPointerException("No null stylesheet accepted");
      }
      properties.addValues(PropertyType.STYLESHEET, stylesheets);
      return this;
    }


    public Iterable<Map.Entry<String, String>> getMetaTags() {
      Iterable<Map.Entry<String, String>> metas = properties.getValues(PropertyType.META_TAG);
      return metas != null ? metas : Tools.<Map.Entry<String, String>>emptyIterable();
    }

    public Render withMetaTag(String name, String value) {
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
      return "Response.Render[]";
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

  public static Render ok(CharSequence content) {
    return content(200, content);
  }

  public static Render notFound(CharSequence content) {
    return content(404, content);
  }

  public static Render content(int code, CharSequence content) {
    return content(code, new Streamable.CharSequence(content));
  }

  public static Render content(int code, Streamable content) {
    return new Render(code, content);
  }

  private static Render content(int code, String mimeType, CharSequence content) {
    return new Render(code, new Streamable.CharSequence(content)).withMimeType(mimeType);
  }

  public static Content ok(InputStream content) {
    return content(200, null, content);
  }

  public static Content notFound(InputStream content) {
    return content(404, null, content);
  }

  public static Content content(int code, InputStream content) {
    return content(code, null, content);
  }

  private static Content content(int code, String mimeType, InputStream content) {
    return new Content(code, new Streamable.InputStream(content)).withMimeType(mimeType);
  }

  public static Error error(Throwable t) {
    return new Error(t);
  }

  public static Error error(String msg) {
    return new Error(msg);
  }
}
