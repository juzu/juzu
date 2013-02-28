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

package examples.tutorial;

import org.xml.sax.InputSource;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.URLEncoder;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WeatherService {

  /** A cache for temperatures. */
  private final HashMap<String, String> cache = new HashMap<String, String>();

  public WeatherService() {
    System.out.println("aezfzef");
  }

  /**
   * Returns the temperature for the specifed location in celsius degrees.
   *
   * @param location the location
   * @return the temperature
   */
  public String getTemperature(String location) {
    String temperature = cache.get(location);
    if (temperature == null) {
      cache.put(location, temperature = retrieveTemperature(location));
    }
    return temperature;
  }

  private String getValue(String url, String xpath) throws Exception {
    XPathExpression expr = XPathFactory.
        newInstance().newXPath().compile(xpath);
    InputSource src = new InputSource(url);
    return expr.evaluate(src);
  }

  /**
   * Retrieve the temperature.
   *
   * @param location the location
   * @return the temperature
   */
  protected String retrieveTemperature(String location) {
    try {
      // First we get the location WOEID
      String woeidURL =
          "http://query.yahooapis.com/v1/public/yql" +
              "?q=select%20*%20from%20geo.places%20where%20text%3D%22" +
              URLEncoder.encode(location, "UTF-8") +
              "%22&format=xml";
      String woeid = getValue(woeidURL, "//*[local-name()='woeid']/text()");

      // Now get weather temperature
      String weatherURL =
          "http://weather.yahooapis.com/forecastrss?w=" +
              URLEncoder.encode(woeid, "UTF-8") +
              "&u=c";
      return getValue(weatherURL, "//*[local-name()='condition']/@temp");
    }
    catch (Exception e) {
      // Unavailable
      return "?";
    }
  }
}
