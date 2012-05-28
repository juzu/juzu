/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.spi.request.portlet;

import org.juzu.Response;
import org.juzu.asset.Asset;
import org.juzu.impl.inject.ScopedContext;
import org.juzu.impl.spi.request.RenderBridge;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderBridge extends PortletMimeBridge<RenderRequest, RenderResponse> implements RenderBridge
{

   public PortletRenderBridge(PortletBridgeContext context, RenderRequest request, RenderResponse response, boolean buffer)
   {
      super(context, request, response, buffer);
   }

   public void setTitle(String title)
   {
      resp.setTitle(title);
   }

   @Override
   public void end(Response response) throws IllegalStateException, IOException
   {
      // Improve that because it will not work on streaming portals...
      // for now it's OK
      if (response instanceof Response.Content.Render)
      {
         Response.Content.Render render = (Response.Content.Render)response;

         // For now only in gatein since liferay won't support it very well
         if (req.getPortalContext().getPortalInfo().startsWith("GateIn Portlet Container") || true)
         {
            Iterable<Asset.Literal> scripts = context.assetManager.resolveAssets(render.getScripts());
            Iterable<Asset.Literal> stylesheets = context.assetManager.resolveAssets(render.getStylesheets());

            //
            for (Asset.Literal stylesheet : stylesheets)
            {
               int pos = stylesheet.getURI().lastIndexOf('.');
               String ext = pos == -1 ? "css" : stylesheet.getURI().substring(pos + 1);
               Element elt = this.resp.createElement("link");
               elt.setAttribute("media", "screen");
               elt.setAttribute("rel", "stylesheet");
               elt.setAttribute("type", "text/" + ext);
               elt.setAttribute("href", getAssetURL(stylesheet));
               this.resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
            }

            //
            for (Asset.Literal script : scripts)
            {
               String url = getAssetURL(script);
               Element elt = this.resp.createElement("script");
               elt.setAttribute("type", "text/javascript");
               elt.setAttribute("src", url);
               // This comment is needed for liferay to make the script pass the minifier
               // it forces to have a <script></script> tag
               Comment comment = elt.getOwnerDocument().createComment(request.getApplication().getName() + " script ");
               elt.appendChild(comment);
               this.resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
            }
         }

         //
         String title = render.getTitle();
         if (title != null)
         {
            setTitle(title);
         }
      }

      //
      super.end(response);
   }

   private String getAssetURL(Asset.Literal asset)
   {
      StringBuilder sb = new StringBuilder();
      String uri = asset.getURI();
      switch (asset.getLocation())
      {
         case SERVER:
            sb.append(req.getContextPath());
            if (!uri.startsWith("/"))
            {
               sb.append('/');
            }
            sb.append(uri);
            break;
         case CLASSPATH:
            sb.append(req.getContextPath()).append("/assets");
            if (!uri.startsWith("/"))
            {
               sb.append('/');
            }
            sb.append(uri);
            break;
         case EXTERNAL:
            sb.append(uri);
            break;
      }
      return sb.toString();
   }
   
   private String getAssetURL(String url)
   {
      if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))
      {
         return url;
      }
      else
      {
         return req.getContextPath() + "/" + url;
      }
   }

   @Override
   public void close()
   {
      ScopedContext context = getFlashContext(false);
      if (context != null)
      {
         context.close();
      }
   }
}
