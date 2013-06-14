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
package juzu.impl.bridge;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.asset.Asset;
import juzu.impl.common.Tools;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.io.StreamableDecorator;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ViewStreamable extends StreamableDecorator {

  /** . */
  public static final PropertyType<Asset> STYLESHEET_ASSET = new PropertyType<Asset>() {};

  /** . */
  public static final PropertyType<Asset> SCRIPT_ASSET = new PropertyType<Asset>() {};

  /** . */
  private final Response.Content content;

  public ViewStreamable(Response.Content content) {
    super(content.getStreamable());

    this.content = content;
  }

  @Override
  protected void sendHeader(Stream consumer) {

    //
    PropertyMap properties = content.getProperties();

    //
    StringWriter writer = new StringWriter();

    //
    try {
      writer.append("<!DOCTYPE html>\n");
      writer.append("<html>\n");
      writer.append("<head>\n");

      //
      String title = properties.getValue(PropertyType.TITLE);
      if (title != null) {
        writer.append("<title>");
        writer.append(title);
        writer.append("</title>\n");
      }

      //
      Iterable<Map.Entry<String, String>> metaProps = properties.getValues(PropertyType.META_TAG);
      if (metaProps != null) {
        for (Map.Entry<String, String> meta : metaProps) {
          writer.append("<meta name=\"");
          writer.append(meta.getKey());
          writer.append("\" content=\"");
          writer.append(meta.getValue());
          writer.append("\">\n");
        }
      }

      //
      Iterable<Asset> stylesheets = properties.getValues(STYLESHEET_ASSET);
      if (stylesheets != null) {
        for (Asset stylesheet : stylesheets) {
          String path = stylesheet.getURI();
          int pos = path.lastIndexOf('.');
          String ext = pos == -1 ? "css" : path.substring(pos + 1);
          writer.append("<link rel=\"stylesheet\" type=\"text/");
          writer.append(ext);
          writer.append("\" href=\"");
          renderAssetURL(stylesheet.getLocation(), stylesheet.getURI(), writer);
          writer.append("\"></link>\n");
        }
      }

      //
      Iterable<Asset> scripts = properties.getValues(SCRIPT_ASSET);
      if (scripts != null) {
        for (Asset script : scripts) {
          writer.append("<script type=\"text/javascript\" src=\"");
          renderAssetURL(script.getLocation(), script.getURI(), writer);
          writer.append("\"></script>\n");
        }
      }

      // Encode header tag
      Iterable<Element> headers = properties.getValues(PropertyType.HEADER_TAG);
      if (headers != null) {
        for (Element header : headers) {
          Tools.encodeHtml(header, writer);
        }
      }

      //
      writer.append("</head>\n");
      writer.append("<body>\n");
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    //
    consumer.provide(Chunk.create(writer.getBuffer()));
  }

  @Override
  protected void sendFooter(Stream consumer) {
    consumer.provide(Chunk.create("</body>\n</html>\n"));
  }

  public abstract void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException;
}
