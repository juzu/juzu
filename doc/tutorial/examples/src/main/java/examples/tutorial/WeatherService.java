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
