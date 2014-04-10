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

package org.sample.booking.controllers;

import juzu.Action;
import juzu.Path;
import juzu.PropertyType;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import org.sample.booking.Flash;
import org.sample.booking.models.Booking;
import org.sample.booking.models.Hotel;
import org.sample.booking.models.User;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Hotels // extends Application
{

/*
   @Before
   static void checkUser() {
       if(connected() == null) {
           flash.error("Please log in first");
           Application.index();
       }
   }

   // ~~~
*/

  @Inject
  Login login;

  @Inject
  Flash flash;

  @Inject
  @Path("hotels/index.gtmpl")
  org.sample.booking.templates.hotels.index index;

  @Inject
  @Path("hotels/list.gtmpl")
  org.sample.booking.templates.hotels.list list;

  @Inject
  @Path("hotels/book.gtmpl")
  org.sample.booking.templates.hotels.book book;

  @Inject
  @Path("hotels/show.gtmpl")
  org.sample.booking.templates.hotels.show show;

  @Inject
  @Path("hotels/confirmBooking.gtmpl")
  org.sample.booking.templates.hotels.confirmBooking confirmBooking;

  public Response.Content index() {
    String username = login.getUserName();
    List<Booking> bookings = Booking.findByUser(username);
    return index.with().bookings(bookings).ok();
  }

  @Ajax
  @Resource
  @Route("/hotels")
  public Response.Content list(String search, String size, String page) {
    int _size = size != null ? Integer.parseInt(size) : 5;
    int _page = page != null ? Integer.parseInt(page) : 0;
    List<Hotel> hotels;
    Pattern pattern;
    if (search != null && search.trim().length() > 0) {
      pattern = Pattern.compile(".*" + Pattern.quote(search.trim()) + ".*", Pattern.CASE_INSENSITIVE);
    }
    else {
      pattern = Pattern.compile(".*");
    }
    hotels = Hotel.find(pattern, pattern, _size, _page);
    return list.with().hotels(hotels).page(_page).ok();
  }

  @View
  @Route("/hotels/{id}")
  public Response.Content show(String id) {
    Hotel hotel = Hotel.findById(id);
    return show.with().hotel(hotel).ok();
  }

  @View
  @Route("/hotels/{id}/booking")
  public Response.Content book(String id, Booking booking) {
    Hotel hotel = Hotel.findById(id);
    if (booking == null) {
      booking = new Booking();
    }
    return book.with().hotel(hotel).booking(booking).ok();
  }

  @Action
  @Route("/hotels/{id}/booking")
  public Response processConfirmBooking(String confirm, String id, String revise, @Valid Booking booking) {
    Hotel hotel = Hotel.findById(id);
    User user = User.find(login.getUserName(), null);
    booking.hotel = hotel;
    booking.user = user;

//      validation.valid(booking);

    // Errors or revise
    if (/*validation.hasErrors() || */revise != null) {
      return Hotels_.book(id, booking);
    }
    else if (confirm != null) {
      // Confirm
      booking.create();
      flash.setSuccess("Thank you, " + login.getUserName() + ", your confimation number for " + hotel.name
        + " is " + booking.id);
      return Application_.index();
    }
    else {
      // Display booking
      return Hotels_.confirmBooking(id, booking).with(PropertyType.REDIRECT_AFTER_ACTION, false);
    }
  }

  @View
  @Route("/hotels/{id}/confirm")
  public Response.Content confirmBooking(String id, Booking booking) {
    Hotel hotel = Hotel.findById(id);
    return confirmBooking.with().total(0).hotel(hotel).booking(booking).ok();
  }

  @Action
  @Route("/bookings/{id}")
  public Response cancelBooking(String id) {
    Booking booking = Booking.find(id);
    booking.delete();
    flash.setSuccess("Booking cancelled for confirmation number " + id);
    return Application_.index();
  }

  @View
  @Route("/settings")
  public void settings() {
    throw new UnsupportedOperationException("todo settings.gtmpl");
  }
/*
   public static void saveSettings(String password, String verifyPassword) {
       User connected = connected();
       connected.password = password;
       validation.valid(connected);
       validation.required(verifyPassword);
       validation.equals(verifyPassword, password).message("Your password doesn't match");
       if(validation.hasErrors()) {
           render("@settings", connected, verifyPassword);
       }
       connected.save();
       flash.success("Password updated");
       index();
   }
*/
}
