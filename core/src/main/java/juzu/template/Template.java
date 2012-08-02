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

package juzu.template;

import juzu.PropertyMap;
import juzu.Response;
import juzu.UndeclaredIOException;
import juzu.impl.request.Request;
import juzu.impl.plugin.template.TemplatePlugin;
import juzu.impl.common.Path;
import juzu.io.AppendableStream;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.request.MimeContext;
import juzu.request.RequestContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p></p>A template as seen by an application. A template is identified by its {@link #path} and can used to produce markup.
 * Templates perform rendering using a parameter map and a locale as inputs and produces a markup response.</p>
 *
 * <p>Template can be rendered with many methods that will affect the current execution. Those methods will implicitly set
 * the produced markup response on the {@link MimeContext} using the {@link MimeContext#setResponse(juzu.Response.Content)}
 * method: {@link #render()}, {@link #render(java.util.Locale)}, {@link #render(java.util.Map)}, {@link #render(java.util.Map, java.util.Locale)},
 * {@link #notFound()}, {@link #notFound(java.util.Locale)}, {@link #notFound(java.util.Map)}, {@link #notFound(java.util.Map, java.util.Locale)},
 * {@link #ok()}, {@link #ok(java.util.Locale)}, {@link #ok(java.util.Map)}, {@link #ok(java.util.Map, java.util.Locale)}.</p>
 *
 * <p>Template can be parameterized using a fluent API with the {@link Builder} object provided by the {@link #with()}
 * method:
 * <br/>
 * <br/>
 * <code>template.with().set("date", new java.util.Date()).render()</code>
 * <br/>
 * <br/>
 * The template compiler produces also a subclass of the template that can be used instead of this base template class.
 * This sub class overrides the {@link #with()} method to return a builder that provides typed methods when the
 * template declares parameters:
 * <br/>
 * <br/>
 * <code>template.with().date(new java.util.Date()).render()</code>
 * </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Template {

  /** . */
  private final Path path;

  /** . */
  private final TemplatePlugin applicationContext;

  public Template(TemplatePlugin applicationContext, String path) {
    this(applicationContext, Path.parse(path));
  }

  public Template(TemplatePlugin applicationContext, Path path) {
    this.applicationContext = applicationContext;
    this.path = path;
  }

  /**
   * Returns the template path.
   *
   * @return the temlate path
   */
  public final Path getPath() {
    return path;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() + "[path=" + path + "]";
  }

  /**
   * Renders the template and returns a render response.
   *
   * @return the render response
   */
  public Response.Render render() throws TemplateExecutionException, UndeclaredIOException {
    return render(null, null);
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param locale     the locale
   * @return the render response
   */
  public Response.Render render(Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    return render(null, locale);
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @return the render response
   */
  public Response.Render render(Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    return render(parameters, null);
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @param locale     the locale
   * @return the render response
   */
  public Response.Render render(final Map<String, ?> parameters, final Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    try {
      RequestContext context = Request.getCurrent().getContext();
      if (context instanceof MimeContext) {
        MimeContext mime = (MimeContext)context;
        PropertyMap properties = new PropertyMap();
        TemplateRenderContext streamable = applicationContext.render(Template.this, properties, parameters, locale);
        StringBuilder sb = new StringBuilder();
        streamable.render(new AppendableStream(sb));
        Response.Render render = new Response.Content.Render(properties, new Streamable.CharSequence(sb));
        mime.setResponse(render);
        return render;
      }
      else {
        throw new AssertionError("does not make sense");
      }
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  /**
   * Renders the template and set the response on the current {@link MimeContext}.
   *
   * @return the ok resource response
   */
  public final Response.Content ok() {
    return ok(null, null);
  }

  /**
   * Renders the template and set the response on the current {@link MimeContext}.
   *
   * @param locale     the locale
   * @return the ok resource response
   */
  public final Response.Content ok(Locale locale) {
    return ok(null, locale);
  }

  /**
   * Renders the template and set the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @return the ok resource response
   */
  public final Response.Content ok(Map<String, ?> parameters) {
    return ok(parameters, null);
  }

  /**
   * Renders the template and set the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @param locale     the locale
   * @return the ok resource response
   */
  public final Response.Content<Stream.Char> ok(Map<String, ?> parameters, Locale locale) {
    StringBuilder sb = new StringBuilder();
    renderTo(new AppendableStream(sb), parameters, locale);
    return Response.ok(sb.toString());
  }

  /**
   * Renders the template and set a 404 response on the current {@link MimeContext}.
   *
   * @return the not found resource response
   */
  public final Response.Content notFound() {
    return notFound(null, null);
  }

  /**
   * Renders the template and set a 404 response on the current {@link MimeContext}.
   *
   * @param locale     the locale
   * @return the not found resource response
   */
  public final Response.Content notFound(Locale locale) {
    return notFound(null, locale);
  }

  /**
   * Renders the template and set a 404 response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @return the not found resource response
   */
  public final Response.Content notFound(Map<String, ?> parameters) {
    return notFound(parameters, null);
  }

  /**
   * Renders the template and set a 404 response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @param locale     the locale
   * @return the not found resource response
   */
  public final Response.Content<Stream.Char> notFound(Map<String, ?> parameters, Locale locale) {
    StringBuilder sb = new StringBuilder();
    renderTo(new AppendableStream(sb), parameters, locale);
    return Response.content(404, sb.toString());
  }

  /**
   * Renders the template to the specified appendable, the current {@link MimeContext} will not be affected.
   *
   * @param appendable the appendable
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable) throws TemplateExecutionException, UndeclaredIOException {
    return renderTo(appendable, Collections.<String, Object>emptyMap(), null);
  }

  /**
   * Renders the template to the specified appendable, the current {@link MimeContext} will not be affected.
   *
   * @param appendable the appendable
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable, Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    return renderTo(appendable, Collections.<String, Object>emptyMap(), locale);
  }

  /**
   * Renders the template to the specified appendable, the current {@link MimeContext} will not be affected.
   *
   * @param appendable the appendable
   * @param parameters the attributes
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    return renderTo(appendable, parameters, null);
  }

  /**
   * Renders the template to the specified appendable, the current {@link MimeContext} will not be affected.
   *
   * @param appendable the appendable
   * @param parameters the attributes
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(
    A appendable,
    Map<String, ?> parameters,
    Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    if (appendable == null) {
      throw new NullPointerException("No null appendable can be provided");
    }

    // Delegate rendering
    renderTo(new AppendableStream(appendable), parameters, locale);

    //
    return appendable;
  }

  /**
   * Renders the template to the specified printer, the current {@link MimeContext} will not be affected.
   *
   * @param printer    the printer
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public void renderTo(Stream.Char printer) throws TemplateExecutionException, UndeclaredIOException {
    renderTo(printer, Collections.<String, Object>emptyMap(), null);
  }

  /**
   * Renders the template to the specified printer, the current {@link MimeContext} will not be affected.
   *
   * @param printer    the printer
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception

   */
  public void renderTo(Stream.Char printer, Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    renderTo(printer, Collections.<String, Object>emptyMap(), locale);
  }

  /**
   * Renders the template to the specified printer, the current {@link MimeContext} will not be affected.
   *
   * @param printer    the printer
   * @param parameters the attributes
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public void renderTo(Stream.Char printer, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    renderTo(printer, parameters, null);
  }

  /**
   * Renders the template to the specified printer, the current {@link MimeContext} will not be affected.
   *
   * @param printer    the printer
   * @param parameters the attributes
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public void renderTo(
    Stream.Char printer,
    Map<String, ?> parameters,
    Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    if (printer == null) {
      throw new NullPointerException("No null printe provided");
    }
    try {
      TemplateRenderContext trc = applicationContext.render(this, null, parameters, locale);
      trc.render(printer);
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  /**
   * Returns a builder to further customize the template rendering.
   *
   * @return a new builder instance
   */
  public abstract Builder with();

  /**
   * Returns a builder to further customize the template rendering.
   *
   * @return a new builder instance
   */
  public Builder with(Locale locale) {
    Builder builder = with();
    builder.locale = locale;
    return builder;
  }

  /**
   * A builder providing a fluent syntax for rendering a template.
   */
  public class Builder {

    /** The parameters. */
    private Map<String, Object> parameters;

    /** The locale. */
    private Locale locale;

    /**
     * Update a parameter, if the value is not null the parameter with the specified name is set, otherwise the
     * parameter is removed. If the parameter is set and a value was set previously, the old value is overwritten
     * otherwise. If the parameter is removed and the value does not exist, nothing happens.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this builder
     * @throws NullPointerException if the name argument is null
     */
    public Builder set(String name, Object value) throws NullPointerException {
      if (name == null) {
        throw new NullPointerException("The parameter argument cannot be null");
      }
      if (value != null) {
        if (parameters == null) {
          parameters = new HashMap<String, Object>();
        }
        parameters.put(name, value);
      }
      else if (parameters != null) {
        parameters.remove(name);
      }
      return this;
    }

    /**
     * Renders the template and returns a render response.
     *
     * @return the render response
     */
    public Response.Render render() throws TemplateExecutionException, UndeclaredIOException {
      return Template.this.render(parameters, locale);
    }

    /**
     * Renders the template and set the response on the current {@link MimeContext}.
     *
     * @return the ok resource response
     */
    public final Response.Content ok() {
      return Template.this.ok(parameters, locale);
    }

    /**
     * Renders the template and set a 404 response on the current {@link MimeContext}.
     *
     * @return the not found resource response
     */
    public final Response.Content notFound() {
      return Template.this.notFound(parameters, locale);
    }

    /**
     * Renders the template to the specified appendable, the current {@link MimeContext} will not be affected.
     *
     * @param appendable the appendable
     * @throws TemplateExecutionException any execution exception
     * @throws UndeclaredIOException      any io exception
     */
    public <A extends Appendable> A renderTo(A appendable) throws TemplateExecutionException, UndeclaredIOException {
      return Template.this.renderTo(appendable, parameters, locale);
    }

    /**
     * Renders the template to the specified printer, the current {@link MimeContext} will not be affected.
     *
     * @param printer    the printer
     * @throws TemplateExecutionException any execution exception
     * @throws UndeclaredIOException      any io exception
     */
    public void renderTo(Stream.Char printer) throws TemplateExecutionException, UndeclaredIOException {
      Template.this.renderTo(printer, parameters, locale);
    }
  }
}
