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

package juzu.template;

import juzu.PropertyMap;
import juzu.Response;
import juzu.io.UndeclaredIOException;
import juzu.impl.plugin.application.Application;
import juzu.impl.request.Request;
import juzu.impl.plugin.template.TemplatePlugin;
import juzu.impl.common.Path;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.template.spi.juzu.dialect.gtmpl.MessageKey;
import juzu.io.Streams;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.request.ApplicationContext;
import juzu.request.MimeContext;
import juzu.request.RequestContext;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
  private final TemplatePlugin plugin;

  /** . */
  private final Class<? extends TemplateStub> stubType;

  @Inject
  Application application;

  public Template(TemplatePlugin plugin, String path, Class<? extends TemplateStub> stubType) {
    this(plugin, Path.parse(path), stubType);
  }

  public Template(TemplatePlugin plugin, Path path, Class<? extends TemplateStub> stubType) {
    this.plugin = plugin;
    this.path = path;
    this.stubType = stubType;
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
   * Renders the template and set the response on the current {@link MimeContext}
   */
  public void render() throws TemplateExecutionException, UndeclaredIOException {
    with().render();
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param locale     the locale
   */
  public void render(Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    with(locale).render();
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   */
  public void render(Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    with(parameters).render();
  }

  /**
   * Renders the template and set a the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @param locale     the locale
   */
  public void render(final Map<String, ?> parameters, final Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    with(parameters).locale(locale).render();
  }

  /**
   * Renders the template.
   *
   * @return the ok resource response
   */
  public final Response.Render ok() {
    return with().ok();
  }

  /**
   * Renders the template.
   *
   * @param locale     the locale
   * @return the ok resource response
   */
  public final Response.Render ok(Locale locale) {
    return with(locale).ok();
  }

  /**
   * Renders the template and set the response on the current {@link MimeContext}.
   *
   * @param parameters the parameters
   * @return the ok resource response
   */
  public final Response.Render ok(Map<String, ?> parameters) {
    return with(parameters).ok();
  }

  /**
   * Renders the template.
   *
   * @param parameters the parameters
   * @param locale     the locale
   * @return the ok resource response
   */
  public final Response.Render ok(Map<String, ?> parameters, Locale locale) {
    return with(parameters).locale(locale).ok();
  }

  /**
   * Renders the template.
   *
   * @return the not found resource response
   */
  public final Response.Render notFound() {
    return notFound(null, null);
  }

  /**
   * Renders the template.
   *
   * @param locale     the locale
   * @return the not found resource response
   */
  public final Response.Render notFound(Locale locale) {
    return notFound(null, locale);
  }

  /**
   * Renders the template.
   *
   * @param parameters the parameters
   * @return the not found resource response
   */
  public final Response.Render notFound(Map<String, ?> parameters) {
    return notFound(parameters, null);
  }

  /**
   * Renders the template.
   *
   * @param parameters the parameters
   * @param locale     the locale
   * @return the not found resource response
   */
  public final Response.Render notFound(Map<String, ?> parameters, Locale locale) {
    return with(parameters).locale(locale).notFound();
  }

  /**
   * Renders the template to the specified appendable.
   *
   * @param appendable the appendable
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable) throws TemplateExecutionException, UndeclaredIOException {
    return with().renderTo(appendable);
  }

  /**
   * Renders the template to the specified appendable.
   *
   * @param appendable the appendable
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable, Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    return with(locale).renderTo(appendable);
  }

  /**
   * Renders the template to the specified appendable.
   *
   * @param appendable the appendable
   * @param parameters the attributes
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public <A extends Appendable> A renderTo(A appendable, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    return with(parameters).renderTo(appendable);
  }

  /**
   * Renders the template to the specified printer.
   *
   * @param printer    the printer
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public void renderTo(Stream.Char printer) throws TemplateExecutionException, UndeclaredIOException {
    with().renderTo(printer);
  }

  /**
   * Renders the template to the specified printer.
   *
   * @param printer    the printer
   * @param locale     the locale
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception

   */
  public void renderTo(Stream.Char printer, Locale locale) throws TemplateExecutionException, UndeclaredIOException {
    with(locale).renderTo(printer);
  }

  /**
   * Renders the template to the specified printer.
   *
   * @param printer    the printer
   * @param parameters the attributes
   * @throws TemplateExecutionException any execution exception
   * @throws UndeclaredIOException      any io exception
   */
  public void renderTo(Stream.Char printer, Map<String, ?> parameters) throws TemplateExecutionException, UndeclaredIOException {
    with(parameters).renderTo(printer);
  }

  /**
   * Create a new builder.
   *
   * @return a new builder instance
   */
  protected abstract Builder builder();

  /**
   * Returns a builder to further customize the template rendering.
   *
   * @return a new builder instance
   */
  public Builder with() {
    return builder();
  }

  /**
   * Returns a builder to further customize the template rendering.
   *
   * @return a new builder instance
   */
  public Builder with(Map<String, ?> parameters) {
    Builder builder = with();
    builder.parameters = (Map<String, Object>)parameters;
    return builder;
  }

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

    private Locale computeLocale() {
      if (locale == null) {
        return Request.getCurrent().getContext().getUserContext().getLocale();
      } else {
        return locale;
      }
    }

    private void doRender(PropertyMap properties, Stream.Char stream) {
      try {

        // Get the specified locale or the current user's one
        final Locale locale = computeLocale();

        //
        TemplateStub stub = plugin.resolveTemplateStub(path);
        if (stub == null) {
          throw new UnsupportedOperationException("Handle me gracefully: couldn't get stub for template " + path);
        }

        //
        TemplateRenderContext context = new TemplateRenderContext(
            stub,
            properties,
            parameters,
            locale
        ) {

          /** . */
          ResourceBundle bundle = null;

          /** . */
          boolean bundleLoaded = false;

          @Override
          public TemplateStub resolveTemplate(String path) {
            return plugin.resolveTemplateStub(path);
          }

          @Override
          public Object resolveBean(String expression) throws InvocationTargetException {
            return application.resolveBean(expression);
          }

          @Override
          public String resolveMessage(MessageKey key) {

            // Lazy load the bundle here
            if (!bundleLoaded) {
              bundleLoaded = true;
              if (locale != null) {
                ApplicationContext applicationContext = Request.getCurrent().getContext().getApplicationContext();
                if (applicationContext != null) {
                  bundle = applicationContext.resolveBundle(locale);
                }
              }
            }

            //
            String value = null;
            if (bundle != null) {
              try {
                value = bundle.getString(key.getValue());
              }
              catch (MissingResourceException notFound) {
                // System.out.println("Could not resolve message " + key.getValue());
              }
            }
            return value != null ? value : "";
          }
        };

        //
        context.render(stream);
      }
      catch (IOException e) {
        throw new UndeclaredIOException(e);
      }
    }

    /**
     * Update the locale.
     *
     * @param locale the new locale
     * @return this builder
     */
    public Builder locale(Locale locale) {
      this.locale = locale;
      return this;
    }

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
     * Renders the template and set the response on the current {@link MimeContext}.
     *
     * @return the ok resource response
     */
    public final Response.Render ok() {
      return status(200);
    }

    /**
     * Renders the template and returns a response with the not found status.
     *
     * @return the not found response
     */
    public final Response.Render notFound() {
      return status(404);
    }

    /**
     * Renders the template and returns a response with the specified status.
     *
     * @return the response
     */
    public final Response.Render status(int status) {
      StringBuilder sb = new StringBuilder();
      PropertyMap properties = new PropertyMap();
      doRender(properties, Streams.appendable(sb));
      return new Response.Render(status, properties,  new Streamable.CharSequence(sb));
    }

    /**
     * Renders the template to the specified appendable.
     *
     * @param appendable the appendable
     * @throws TemplateExecutionException any execution exception
     * @throws UndeclaredIOException      any io exception
     */
    public <A extends Appendable> A renderTo(A appendable) throws TemplateExecutionException, UndeclaredIOException {
      renderTo(Streams.appendable(appendable));
      return appendable;
    }

    /**
     * Renders the template to the specified printer.
     *
     * @param printer    the printer
     * @throws TemplateExecutionException any execution exception
     * @throws UndeclaredIOException      any io exception
     */
    public void renderTo(Stream.Char printer) throws TemplateExecutionException, UndeclaredIOException {
      if (printer == null) {
        throw new NullPointerException("No null printe provided");
      }
      doRender(null, printer);
    }

    /**
     * Renders the template and set the response on the current {@link MimeContext}
     */
    public void render() throws TemplateExecutionException, UndeclaredIOException {
      try {
        RequestContext context = Request.getCurrent().getContext();
        if (context instanceof MimeContext) {
          MimeContext mime = (MimeContext)context;
          Response.Render render = status(200);
          mime.setResponse(render);
        }
        else {
          throw new AssertionError("does not make sense");
        }
      }
      catch (IOException e) {
        throw new UndeclaredIOException(e);
      }
    }
  }
}
