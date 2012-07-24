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

package examples.tutorial.weather3;

import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WeatherService {

  public String getTemperature(String location) {
    return getTemperature(location, "c");
  }

  public String getTemperature(String location, String grade) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      XPathExpression expr = xpath.compile("//temp_" + grade + "/@data");
      String url = "http://www.google.com/ig/api?weather=" + location;
      InputSource src = new InputSource(url);
      src.setEncoding("ISO-8859-1");
      return expr.evaluate(src);
    }
    catch (XPathExpressionException e) {
      return "unavailable";
    }
  }
}
