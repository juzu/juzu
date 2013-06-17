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
package juzu.impl.bridge.spi.web;

import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.asset.Asset;
import juzu.impl.asset.AssetManager;
import juzu.impl.common.Tools;
import juzu.io.Chunk;
import juzu.io.Stream;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

/** @author Julien Viet */
public abstract class WebStream implements AsyncStream {

  /** . */
  private static final int BUFFERING = 0;

  /** . */
  private static final int STREAMING = 1;

  /** . */
  private static final int CLOSED = 2;

  /** . */
  private static final int FAILED = 3;

  /** . */
  private final HttpStream stream;

  /** . */
  private int status = BUFFERING;

  /** . */
  private final AssetManager stylesheetManager;

  /** . */
  private final AssetManager scriptManager;

  /** The current document being assembled. */
  private final Page page;

  public WebStream(HttpStream stream, AssetManager stylesheetManager, AssetManager scriptManager) {
    this.stream = stream;
    this.stylesheetManager = stylesheetManager;
    this.scriptManager = scriptManager;
    this.page = new Page();
  }

  public void provide(Chunk chunk) {

    //
    if (status == BUFFERING) {
      if (chunk instanceof Chunk.Property<?>) {
        Chunk.Property<?> property = (Chunk.Property<?>)chunk;
        if (property.type == PropertyType.TITLE) {
          page.title = (String)property.value;
        } else if (property.type == PropertyType.META_TAG) {
          page.metaTags.add(((Map.Entry<String, String>)property.value));
        } else if (property.type == PropertyType.STYLESHEET) {
          page.stylesheets.add(((String)property.value));
        } else if (property.type == PropertyType.SCRIPT) {
          page.scripts.add(((String)property.value));
        } else if (property.type == PropertyType.HEADER_TAG) {
          page.headerTags.add(((Element)property.value));
        } else {
          stream.provide(property);
        }
      } else if (chunk instanceof Chunk.Data) {
        try {
          if (page.stylesheets.size() > 0 && stylesheetManager != null) {
            Iterable<Asset> stylesheetAssets =  stylesheetManager.resolveAssets(page.stylesheets);
            Tools.addAll(page.stylesheetAssets, stylesheetAssets);
          }
          if (page.scripts.size() > 0 && scriptManager != null) {
            Iterable<Asset> scriptAssets =  scriptManager.resolveAssets(page.scripts);
            Tools.addAll(page.scriptAssets, scriptAssets);
          }
          status = STREAMING;
          page.sendHeader(stream);
        }
        catch (IllegalArgumentException e) {

          // Handle that better...
          e.printStackTrace();

          //
          status = FAILED;
          stream.setStatusCode(500);
          page.clear();
          page.sendHeader(stream);
          Response.error(e).result().asStatus(true).streamable.send(new Stream() {
            public void provide(Chunk chunk) {
              stream.provide(chunk);
            }
            public void close(Thread.UncaughtExceptionHandler errorHandler) {
              // Do nothing
              // perhaps we should have a send(stream, boolean close)
              // that would avoid to do that and help to nest stuff
            }
          });
          page.sendFooter(stream);
          return;
        }
      }
    }

    //
    if (status == STREAMING) {
      stream.provide(chunk);
    }
  }

  public abstract String renderAssetURL(AssetLocation location, String uri);

  public void close(Thread.UncaughtExceptionHandler errorHandler) {
    if (status != CLOSED) {
      try {
        if (status == BUFFERING) {
          provide(Chunk.create(""));
        }
        if (status == STREAMING) {
          page.sendFooter(stream);
        }
      }
      finally {
        status = CLOSED;
        stream.close(errorHandler);
      }
    }
  }

  public void end() {
    stream.end();
  }

  private class Page {

    /** . */
    private String title = null;

    /** . */
    private final LinkedList<Map.Entry<String, String>> metaTags = new LinkedList<Map.Entry<String, String>>();

    /** . */
    private final LinkedList<String> stylesheets = new LinkedList<String>();

    /** . */
    private final LinkedList<String> scripts = new LinkedList<String>();

    /** . */
    private final LinkedList<Element> headerTags = new LinkedList<Element>();

    /** . */
    private final LinkedList<Asset> scriptAssets = new LinkedList<Asset>();

    /** . */
    private final LinkedList<Asset> stylesheetAssets = new LinkedList<Asset>();

    void clear() {
      this.title = null;
      this.metaTags.clear();
      this.stylesheetAssets.clear();
      this.scriptAssets.clear();
      this.headerTags.clear();
    }

    void sendHeader(Stream stream) {
      stream.provide(Chunk.create(
          "<!DOCTYPE html>\n" +
              "<html>\n" +
              "<head>\n"));
      if (title != null) {
        stream.provide(Chunk.create("<title>\n"));
        stream.provide(Chunk.create(title));
        stream.provide(Chunk.create("</title>\n"));
      }
      for (Map.Entry<String, String> metaTag : metaTags) {
        stream.provide(Chunk.create("<meta name=\""));
        stream.provide(Chunk.create(metaTag.getKey()));
        stream.provide(Chunk.create("\" content=\""));
        stream.provide(Chunk.create(metaTag.getValue()));
        stream.provide(Chunk.create("\">\n"));
      }
      for (Asset asset : stylesheetAssets) {
        String path = asset.getURI();
        int pos = path.lastIndexOf('.');
        String ext = pos == -1 ? "css" : path.substring(pos + 1);
        String url = renderAssetURL(asset.getLocation(), asset.getURI());
        stream.provide(Chunk.create("<link rel=\"stylesheet\" type=\"text/"));
        stream.provide(Chunk.create(ext));
        stream.provide(Chunk.create("\" href=\""));
        stream.provide(Chunk.create(url));
        stream.provide(Chunk.create("\"></link>\n"));
      }
      for (Asset asset : scriptAssets) {
        String url = renderAssetURL(asset.getLocation(), asset.getURI());
        stream.provide(Chunk.create("<script type=\"text/javascript\" src=\""));
        stream.provide(Chunk.create(url));
        stream.provide(Chunk.create("\"></script>\n"));
      }
      for (Element headerTag : headerTags) {
        try {
          StringBuilder buffer = new StringBuilder();
          Tools.encodeHtml(headerTag, buffer);
          stream.provide(Chunk.create(buffer));
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      stream.provide(Chunk.create(
          "</head>\n" +
              "<body>\n"));
    }

    void sendFooter(Stream stream) {
      stream.provide(Chunk.create(
          "</body>\n" +
              "</html>\n"));
    }
  }
}
