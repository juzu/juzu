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

  public void index() {
    String username = login.getUserName();
    List<Booking> bookings = Booking.findByUser(username);
    index.with().bookings(bookings).render();
  }

  @Ajax
  @Resource
  @Route("/hotels/list")
  public void list(String search, String size, String page) {
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
    list.with().hotels(hotels).page(_page).render();
  }

  @View
  @Route("/hotels/{id}")
  public void show(String id) {
    Hotel hotel = Hotel.findById(id);
    show.with().hotel(hotel).render();
  }

  @View
  @Route("/hotels/{id}/booking")
  public void book(String id, Booking booking) {
    Hotel hotel = Hotel.findById(id);
    if (booking == null) {
      booking = new Booking();
    }
    book.with().hotel(hotel).booking(booking).render();
  }

  @Action
  @Route("/hotels/{id}/booking")
  public Response processConfirmBooking(String confirm, String id, String revise, Booking booking) {
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
  public void confirmBooking(String id, Booking booking) {
    Hotel hotel = Hotel.findById(id);
    confirmBooking.with().total(0).hotel(hotel).booking(booking).render();
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
