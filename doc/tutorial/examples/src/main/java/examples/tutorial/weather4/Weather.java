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

package examples.tutorial.weather4;

import examples.tutorial.WeatherService;
import juzu.Path;
import juzu.Route;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Weather {

  @Inject
  WeatherService weatherService;

  @Inject
  @Path("index.gtmpl")
  Template index;

  @View
  public void index() {
    index("marseille");
  }

  @View
  @Route("/show/{location}")
  public void index(String location) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("location", location);
    parameters.put("temperature", weatherService.getTemperature(location));
    index.render(parameters);
  }
}
