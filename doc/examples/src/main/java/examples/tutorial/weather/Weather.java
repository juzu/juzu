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

package examples.tutorial.weather;

import org.juzu.Path;
import org.juzu.View;
import org.juzu.template.Template;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Weather
{

   private final XPathExpression xpath;

   public Weather() throws XPathException
   {
      xpath = XPathFactory.newInstance().newXPath().compile("//temp_c/@data");
   }

   @Inject
   @Path("index.gtmpl")
   Template index;

   @View
   public void index() throws Exception
   {
      show("marseille");
   }

   @View
   public void show(String location) throws Exception
   {
      String url = "http://www.google.com/ig/api?weather=" + location;
      InputSource src = new InputSource(url);
      src.setEncoding("ISO-8859-1");
      String temperature = xpath.evaluate(src);
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("location", location);
      data.put("temperature", temperature);
      index.render(data);
   }
}
