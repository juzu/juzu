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

package examples.tutorial.weather9;

import examples.tutorial.weather3.WeatherService;
import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.View;
import juzu.plugin.ajax.Ajax;

import javax.inject.Inject;
import javax.portlet.PortletPreferences;
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
  PortletPreferences preferences;

  @Inject
  @Path("index.gtmpl")
  examples.tutorial.weather9.templates.index index;

  @Inject
  @Path("fragment.gtmpl")
  examples.tutorial.weather9.templates.fragment fragment;

  @View
  public void index() {
    index("marseille");
  }

  @View
  public void index(String location) {
    String grade = preferences.getValue("grade", "c");
    index.
      with().
      location(location).
      temperature(weatherService.getTemperature(location, grade)).
      grade(grade).
      locations(locations).
      render();
  }

  @Action
  public Response updateGrade(String grade, String location) throws java.io.IOException,
    javax.portlet.PortletException {
    preferences.setValue("grade", grade);
    preferences.store();
    return Weather_.index(location);
  }

  @Action
  public Response add(String location) {
    if (!locations.contains(location)) {
      locations.add(location);
    }
    return Weather_.index(location);
  }

  @Ajax
  @Resource
  public void getFragment(String location) {
    String grade = preferences.getValue("grade", "c");
    String temperature = weatherService.getTemperature(location, grade);
    fragment.
      with().
      location(location).
      temperature(weatherService.getTemperature(location, grade)).
      grade(grade).
      render();
  }
}
