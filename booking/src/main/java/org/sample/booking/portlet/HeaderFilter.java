package org.sample.booking.portlet;

import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class HeaderFilter implements RenderFilter
{

   public void init(FilterConfig filterConfig) throws PortletException
   {
   }

   public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException, PortletException
   {

      //
      Element jQuery1 = response.createElement("script");
      jQuery1.setAttribute("type", "text/javascript");
      jQuery1.setAttribute("src", request.getContextPath() + "/public/javascripts/jquery-1.3.2.min.js");
      response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jQuery1);

      //
      Element jQuery2 = response.createElement("script");
      jQuery2.setAttribute("type", "text/javascript");
      jQuery2.setAttribute("src", request.getContextPath() + "/public/javascripts/jquery-ui-1.7.2.custom.min.js");
      response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jQuery2);

      //
      Element css1 = response.createElement("link");
      css1.setAttribute("rel", "stylesheet");
      css1.setAttribute("type", "text/css");
      css1.setAttribute("media", "screen");
      css1.setAttribute("href", request.getContextPath() + "/public/stylesheets/main.css");
      response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, css1);

      //
      Element css2 = response.createElement("link");
      css2.setAttribute("rel", "stylesheet");
      css2.setAttribute("type", "text/css");
      css2.setAttribute("media", "screen");
      css2.setAttribute("href", request.getContextPath() + "/public/ui-lightness/jquery-ui-1.7.2.custom.css");
      response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, css2);

      //
      chain.doFilter(request, response);
   }

   public void destroy()
   {
   }
}
