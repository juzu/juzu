/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu;

import juzu.asset.Asset;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.ParameterHashMap;
import juzu.impl.common.ParameterMap;
import juzu.impl.common.Tools;
import juzu.io.Stream;
import juzu.io.Streamable;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;

/**
 * <p>A response object signalling to the portal the action to take after an interaction. This object is usually
 * returned after the invocation of a controller method and instructs the aggregator the action to take.</p> <p/>
 * <h2>Action response</h2> <p/> <h3>Redirection response</h3> <p/> <p>A <code>Response.Process.Action.Redirect</code>
 * response instructs the aggregator to make a redirection to a valid URL after the interaction, this kind of response
 * is created using the factory method {@link Response#redirect(String)}:
 * <code><pre>
 *    return Response.redirect("http://www.exoplatform.org");
 * </pre></code>
 * </p> <p/> <h3>Proceed to render phase</h3> <p/> <p>A <code>Response.Update</code> response instructs the aggreator to
 * proceed to the render phase of a valid view controller, this kind of response can be created using an {@link
 * juzu.request.ActionContext}, however the the preferred way is to use a controller companion class that carries method
 * factories for creating render responses.</p> <p/> <p>Type safe {@link juzu.Response.Update} factory method are
 * generated for each view or resource controller methods. The signature of an render factory is obtained by using the
 * same signature of the controller method.</p> <p/>
 * <code><pre>
 *    public class MyController {
 * <p/>
 *       &#064;Action
 *       public {@link juzu.Response.Update} myAction() {
 *          return MyController_.myRender("hello");
 *       }
 * <p/>
 *       &#064;View
 *       public void myRender(String param) {
 *       }
 *    }
 * </pre></code>
 * <p/> <h2>Mime response</h2> <p/> <p>Mime response are used by the {@link juzu.request.Phase#VIEW} and the {@link
 * juzu.request.Phase#RESOURCE} phases. Both contains a content to be streamed to the client but still they have some
 * noticeable differences.</p> <p/> <p>The {@link juzu.Response.Content} class is the base response class which will
 * work well for the two phases. However the {@link juzu.request.Phase#VIEW} can specify an optional title and the
 * {@link juzu.request.Phase#RESOURCE} can specify an optional status code for the user agent response.</p> <p/>
 * <p>Responses are created using the {@link Response} factory methods such as</p> <p/> <ul> <li>{@link
 * Response#ok} creates an ok response</li> <li>{@link Response#notFound} creates a not found response</li> </ul>
 * <p/> <p>Response can also created from {@link juzu.template.Template} directly:</p> <p/>
 * <code><pre>
 *    public class MyController {
 * <p/>
 *       &#064;Inject &#064;Path("index.gtmpl") {@link juzu.template.Template} index;
 * <p/>
 *       &#064;View
 *       public {@link juzu.Response.Render} myView() {
 *          return index.render();
 *       }
 * <p/>
 *       &#064;Inject &#064;Path("error.gtmpl")  {@link juzu.template.Template} error;
 * <p/>
 *       &#064;Resource
 *       public {@link juzu.Response.Content} myView() {
 *          return error.notFound();
 *       }
 *    }
 * </pre></code>
 * <p/> <p>The {@link juzu.template.Template.Builder} can also create responses:</p>
 * <p/>
 * <code><pre>
 *    public class MyController {
 * <p/>
 *       &#064;Inject &#064;Path("index.gtmpl") index index;
 * <p/>
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

  /**
   * A response instructing to execute a render phase of a controller method after the current interaction.
   */
  public static class Update extends Response {

    /** . */
    private final MethodHandle target;

    /** . */
    private ParameterMap parameterMap;

    public Update(MethodHandle target) {
      this.target = target;
      this.parameterMap = new ParameterHashMap();
    }

    public MethodHandle getTarget() {
      return target;
    }

    public Update setParameter(String name, String value) throws NullPointerException {
      parameterMap.setParameter(name, value);
      return this;
    }

    public Update setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException {
      parameterMap.setParameter(name, value);
      return this;
    }

    public Update setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException {
      parameterMap.setParameters(parameters);
      return this;
    }

    public Map<String, String[]> getParameters() {
      return parameterMap;
    }

    @Override
    public <T> Update with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Update)super.with(propertyType, propertyValue);
    }

    @Override
    public Update withHeader(String name, String... value) {
      return (Update)super.withHeader(name, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Update) {
        Update that = (Update)obj;
        return parameterMap.equals(that.parameterMap) && properties.equals(that.properties);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Response.Update[parameters" + parameterMap + ",properties=" + properties + "]";
    }
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

  public static class Content<S extends Stream> extends Response {

    /** . */
    private int status;

    /** . */
    private final Class<S> kind;

    /** . */
    private Streamable<S> streamable;

    protected Content(int status, Class<S> kind) {
      this.status = status;
      this.kind = kind;
      this.streamable = null;
    }

    protected Content(int status, Class<S> kind, PropertyMap properties) {
      super(properties);

      //
      this.status = status;
      this.kind = kind;
      this.streamable = null;
    }

    protected Content(int status, Class<S> kind, Streamable<S> streamable) {
      this.status = status;
      this.kind = kind;
      this.streamable = streamable;
    }

    protected Content(int status, Class<S> kind, PropertyMap properties, Streamable<S> streamable) {
      super(properties);

      //
      this.status = status;
      this.kind = kind;
      this.streamable = streamable;
    }

    public Class<S> getKind() {
      return kind;
    }

    public Streamable<S> getStreamable() {
      return streamable;
    }

    public String getMimeType() {
      return properties.getValue(PropertyType.MIME_TYPE);
    }

    public Content<S> withMimeType(String mimeType) {
      properties.setValue(PropertyType.MIME_TYPE, mimeType);
      return this;
    }

    @Override
    public Content<S> withHeader(String name, String... value) {
      return (Content<S>)super.withHeader(name, value);
    }

    public Integer getStatus() {
      return status;
    }

    @Override
    public <T> Content with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Content)super.with(propertyType, propertyValue);
    }

    /**
     * Send the response on the stream argument, Juzu invokes it when it needs to render the content object.
     *
     * @param stream the stream for sending the response
     * @throws IOException any io exception
     */
    public void send(S stream) throws IOException {
      streamable.send(stream);
    }
  }

  public static class Render extends Content<Stream.Char> {

    @Override
    public <T> Render with(PropertyType<T> propertyType, T propertyValue) throws NullPointerException {
      return (Render)super.with(propertyType, propertyValue);
    }

    public Render() {
      super(200, Stream.Char.class);
    }

    public Render(PropertyMap properties, Streamable<Stream.Char> streamable) {
      super(200, Stream.Char.class, properties, streamable);
    }

    public Render(Streamable<Stream.Char> streamable) {
      super(200, Stream.Char.class, streamable);
    }

    public String getTitle() {
      return properties.getValue(PropertyType.TITLE);
    }

    public Render withTitle(String title) {
      properties.setValue(PropertyType.TITLE, title);
      return this;
    }

    public Iterable<Asset> getScripts() {
      Iterable<Asset> scripts = properties.getValues(PropertyType.SCRIPT);
      return scripts != null ? scripts : Tools.<Asset>emptyIterable();
    }

    public Render addScript(Asset script) throws NullPointerException {
      if (script == null) {
        throw new NullPointerException("No null script accepted");
      }
      properties.addValue(PropertyType.SCRIPT, script);
      return this;
    }

    public Iterable<Asset> getStylesheets() {
      Iterable<Asset> stylesheets = properties.getValues(PropertyType.STYLESHEET);
      return stylesheets != null ? stylesheets : Tools.<Asset>emptyIterable();
    }

    public Render addStylesheet(Asset stylesheet) throws NullPointerException {
      if (stylesheet == null) {
        throw new NullPointerException("No null stylesheet accepted");
      }
      properties.addValue(PropertyType.SCRIPT, stylesheet);
      return this;
    }

    @Override
    public Render withHeader(String name, String... value) {
      return (Render)super.withHeader(name, value);
    }

    @Override
    public String toString() {
      return "Response.Render[]";
    }
  }

  public static Response.Redirect redirect(String location) {
    return new Response.Redirect(location);
  }

  public static Render render(CharSequence content) {
    return render(null, content);
  }

  public static Render render(String title, CharSequence content) {
    return new Render(new Streamable.CharSequence(content)).withTitle(title);
  }

  public static Content<Stream.Char> ok(CharSequence content) {
    return content(200, content);
  }

  public static Content<Stream.Binary> ok(String mimeType, InputStream content) {
    return content(200, mimeType, content);
  }

  public static Content<Stream.Binary> ok(InputStream content) {
    return ok(null, content);
  }

  public static Content<Stream.Char> notFound(CharSequence content) {
    return content(404, content);
  }

  public static Content<Stream.Char> content(int code, CharSequence content) {
    return content(code, new Streamable.CharSequence(content));
  }

  public static Content<Stream.Char> content(int code, Streamable<Stream.Char> content) {
    return new Content<Stream.Char>(code, Stream.Char.class, content).withMimeType("text/html");
  }

  public static Content<Stream.Binary> content(int code, InputStream content) {
    return content(code, null, content);
  }

  public static Content<Stream.Binary> content(int code, String mimeType, InputStream content) {
    return new Content<Stream.Binary>(code, Stream.Binary.class, new Streamable.InputStream(content)).withMimeType(mimeType);
  }
}
