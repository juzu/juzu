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

package examples.tutorial.weather8;

import examples.tutorial.WeatherService;
import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Weather {

  static List<String> locations = new ArrayList<String>();

  static {
    locations.add("marseille");
    locations.add("paris");
  }

  @Inject
  WeatherService weatherService;

  @Inject
  @Path("index.gtmpl")
  examples.tutorial.weather8.templates.index index;

  @Inject
  @Path("fragment.gtmpl")
  examples.tutorial.weather8.templates.fragment fragment;

  @View
  public void index() {
    index("marseille");
  }

  @View
  @Route("/show/{location}")
  public void index(String location) {
    index.
      with().
      location(location).
      temperature(weatherService.getTemperature(location)).
      locations(locations).
      render();
  }

  @Action
  @Route("/add")
  public Response add(String location) {
    if (!locations.contains(location)) {
      locations.add(location);
    }
    return Weather_.index(location);
  }

  @Ajax
  @Resource
  @Route("/fragment")
  public void getFragment(String location) {
    fragment.
      with().
      location(location).
      temperature(weatherService.getTemperature(location)).
      render();
  }
}
