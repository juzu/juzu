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
import juzu.impl.plugin.amd.ModuleManager;
import juzu.impl.common.Tools;
import juzu.impl.plugin.amd.Module;
import juzu.io.Chunk;
import juzu.io.Stream;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Iterator;
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
  private final AssetManager assetManager;
  
  /** .*/
  private final ModuleManager moduleManager;

  /** The current document being assembled. */
  private final Page page;

  public WebStream(HttpStream stream, AssetManager assetManager, ModuleManager moduleManager) {
    this.stream = stream;
    this.assetManager = assetManager;
    this.moduleManager = moduleManager;
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
        } else if (property.type == PropertyType.META_HTTP_EQUIV) {
          page.metaHttpEquivs.add(((Map.Entry<String, String>)property.value));
        } else if (property.type == PropertyType.ASSET) {
          page.assets.add(((String)property.value));
        } else if (property.type == Module.TYPE) {
          page.modules.add(((Module)property.value));
        } else if (property.type == PropertyType.HEADER_TAG) {
          page.headerTags.add(((Element)property.value));
        } else {
          stream.provide(property);
        }
      } else if (chunk instanceof Chunk.Data) {
        try {
          if (page.assets.size() > 0 && assetManager != null) {
            Iterable<Asset> resolvedAssets =  assetManager.resolveAssets(page.assets);
            Tools.addAll(page.resolvedAssets, resolvedAssets);
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
    private final LinkedList<Map.Entry<String, String>> metaHttpEquivs = new LinkedList<Map.Entry<String, String>>();

    /** . */
    private final LinkedList<String> assets = new LinkedList<String>();

    /** . */
    private final LinkedList<String> scripts = new LinkedList<String>();

    /** . */
    private final LinkedList<Element> headerTags = new LinkedList<Element>();

    /** . */
    private final LinkedList<Asset> resolvedAssets = new LinkedList<Asset>();

    /** .*/
    private final LinkedList<Module> modules = new LinkedList<Module>();

    void clear() {
      this.title = null;
      this.metaTags.clear();
      this.assets.clear();
      this.headerTags.clear();
      this.modules.clear();
    }

    void sendHeader(HttpStream stream) {
      stream.provide(Chunk.create(
          "<!DOCTYPE html>\n" +
              "<html>\n" +
              "<head>\n"));
      if (title != null) {
        stream.provide(Chunk.create("<title>\n"));
        stream.provide(Chunk.create(title));
        stream.provide(Chunk.create("</title>\n"));
      }
      if (stream.mimeType != null) {
        stream.provide(Chunk.create("<meta http-equiv=\"content-type\" content=\""));
        stream.provide(Chunk.create(stream.mimeType));
        stream.provide(Chunk.create("; charset="));
        stream.provide(Chunk.create(stream.encoding.name()));
        stream.provide(Chunk.create("\">\n"));
      }
      for (Map.Entry<String, String> metaTag : metaHttpEquivs) {
        stream.provide(Chunk.create("<meta http-equiv=\""));
        stream.provide(Chunk.create(metaTag.getKey()));
        stream.provide(Chunk.create("\" content=\""));
        stream.provide(Chunk.create(metaTag.getValue()));
        stream.provide(Chunk.create("\">\n"));
      }
      for (Map.Entry<String, String> metaTag : metaTags) {
        stream.provide(Chunk.create("<meta name=\""));
        stream.provide(Chunk.create(metaTag.getKey()));
        stream.provide(Chunk.create("\" content=\""));
        stream.provide(Chunk.create(metaTag.getValue()));
        stream.provide(Chunk.create("\">\n"));
      }
      for (Asset asset : resolvedAssets) {
        if (asset.isStylesheet()) {
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
      }
      if (!modules.isEmpty()) {
        renderAMD(modules, stream);
      }
      for (Asset asset : resolvedAssets) {
        if (asset.isScript()) {
          String url = renderAssetURL(asset.getLocation(), asset.getURI());
          stream.provide(Chunk.create("<script type=\"text/javascript\" src=\""));
          stream.provide(Chunk.create(url));
          stream.provide(Chunk.create("\"></script>\n"));
        }
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
    
    private void renderAMD(Iterable<Module> modules, Stream stream) {
      StringBuilder buffer = new StringBuilder();
      buffer.append("<script type=\"text/javascript\">");
      buffer.append(" var require={");
      buffer.append("\"paths\":{");
      for (Iterator<Module> i = modules.iterator(); i.hasNext();) {
        Module path = i.next();
        buffer.append("\"").append(path.getId()).append("\":\"");
        String uri = path.getUri();
        uri = uri.substring(0, uri.lastIndexOf(".js"));
        buffer.append(renderAssetURL(path.getLocation(), uri));
        buffer.append("\"");
        if (i.hasNext()) {
          buffer.append(",");
        }
      }
      buffer.append("}");
      buffer.append("};");
      buffer.append("</script>");
      stream.provide(Chunk.create(buffer));
    }
  }
}
