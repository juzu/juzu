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

package juzu.impl.bridge.spi.portlet;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.plugin.asset.AssetService;
import juzu.impl.request.ContextualParameter;
import juzu.request.Phase;
import juzu.io.Chunk;
import juzu.io.Stream;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> {

  PortletMimeBridge(Bridge bridge, Phase phase, Rq request, Rs response, PortletConfig config) {
    super(bridge, phase, request, response, config);
  }

  public abstract Stream createStream(String mimeType, Charset charset) throws IOException;

  @Override
  public Map<ContextualParameter, Object> getContextualArguments(Set<ContextualParameter> parameters) {
    return Collections.emptyMap();
  }

  @Override
  public void send() throws IOException, PortletException {
    if (response instanceof Response.Status) {

      //
      Response.Status status = (Response.Status)response;

      //
      final AssetService assetPlugin = (AssetService)bridge.getApplication().getPlugin("asset");

      //
      Stream stream = new Stream() {

        /** . */
        private Charset charset = Tools.ISO_8859_1;

        /** . */
        private String mimeType = null;

        /** . */
        private Stream dataStream = null;

        /** . */
        private final LinkedList<String> assets = new LinkedList<String>();

        public void provide(Chunk chunk) {
          if (chunk instanceof Chunk.Property) {
            Chunk.Property<?> property = (Chunk.Property<?>)chunk;
            if (property.type == PropertyType.ENCODING) {
              charset = (Charset)property.value;
            } else if (property.type == PropertyType.MIME_TYPE) {
              mimeType = (String)property.value;
            } else if (property.type == PropertyType.HEADER) {
              Map.Entry<String, String[]> header = (Map.Entry<String, String[]>)property.value;
              for (String value : header.getValue()) {
                resp.addProperty(header.getKey(), value);
              }
            } if (property.type == PropertyType.TITLE) {
              if (resp instanceof RenderResponse) {
                ((RenderResponse)resp).setTitle((String)property.value);
              }
            } else if (property.type == PropertyType.META_TAG) {
              Map.Entry<String, String> metaTag = (Map.Entry<String, String>)property.value;
              Element elt = resp.createElement("meta");
              elt.setAttribute("name", metaTag.getKey());
              elt.setAttribute("content", metaTag.getValue());
              resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
            } else if (property.type == PropertyType.ASSET) {
              assets.add(((String)property.value));
            } else if (property.type == PropertyType.HEADER_TAG) {
              Element headerTag = (Element)property.value;
              Element responseTag = resp.createElement(headerTag.getTagName());
              for (Node child : Tools.children(headerTag)) {
                child = responseTag.getOwnerDocument().importNode(headerTag, true);
                responseTag.appendChild(child);
              }
              resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headerTag);
            }
          } else if (chunk instanceof Chunk.Data) {
            Chunk.Data data = (Chunk.Data)chunk;
            if (dataStream == null) {
              Iterable<Asset> resolvedAssets = assetPlugin.getAssetManager().resolveAssets(assets);
              for (Asset resolvedAsset : resolvedAssets) {
                Element elt;
                if (resolvedAsset.isStylesheet()) {
                  int pos = resolvedAsset.getURI().lastIndexOf('.');
                  String ext = pos == -1 ? "css" : resolvedAsset.getURI().substring(pos + 1);
                  elt = resp.createElement("link");
                  elt.setAttribute("media", "screen");
                  elt.setAttribute("rel", "stylesheet");
                  elt.setAttribute("type", "text/" + ext);
                  elt.setAttribute("href", PortletMimeBridge.this.getAssetURL(resolvedAsset));
                } else if (resolvedAsset.isScript()) {
                  String url = PortletMimeBridge.this.getAssetURL(resolvedAsset);
                  elt = resp.createElement("script");
                  elt.setAttribute("type", "text/javascript");
                  elt.setAttribute("src", url);
                  // This comment is needed for liferay to make the script pass the minifier
                  // it forces to have a <script></script> tag
                  String dummy = bridge.getApplication().getName() + " script ";
                  Comment comment = elt.getOwnerDocument().createComment(dummy);
                  elt.appendChild(comment);
                } else {
                  elt = null;
                }
                if (elt != null) {
                  resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
                }
              }
              try {
                dataStream = createStream(mimeType, charset);
              }
              catch (IOException e) {
                throw new UnsupportedOperationException("Handle me gracefully");
              }
            }
            dataStream.provide(data);
          }
        }

        public void close(Thread.UncaughtExceptionHandler errorHandler) {
        }
      };

      //
      if (status.getCode() != 200) {
        resp.addProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status.getCode()));
      }
      status.streamable().send(stream);
    } else if (response instanceof Response.Error) {
      Response.Error error = (Response.Error)response;
      if (bridge.getRunMode().getPrettyFail()) {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.append("<div class=\"juzu\">");
        Throwable cause = error.getCause();
        if (cause != null) {
          Formatting.renderThrowable(null, writer, cause);
        } else {
          writer.append(error.getMessage());
        }
        writer.append("</div>");
        writer.close();
        resp.addProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
      } else {
        throw new PortletException(error.getCause());
      }
    }
  }
}
