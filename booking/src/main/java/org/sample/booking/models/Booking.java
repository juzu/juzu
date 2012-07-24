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

package org.sample.booking.models;

import juzu.Param;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
@Param
public class Booking {

  public String id;

  //    @Required
//     @ManyToOne
  public User user;

  //    @Required
//     @ManyToOne
  public Hotel hotel;

  //    @Required
//     @Temporal(TemporalType.DATE)
//    public Date checkinDate;
  public String checkinDate = "";

  //    @Required
//     @Temporal(TemporalType.DATE)
//    public Date checkoutDate;
  public String checkoutDate = "";

  //    @Required(message="Credit card number is required")
//    @Match(value="^\\d{16}$", message="Credit card number must be numeric and 16 digits long")
  public String creditCard = "";

  //    @Required(message="Credit card name is required")
//    @MinSize(value=3, message="Credit card name is required")
//    @MaxSize(value=70, message="Credit card name is required")
  public String creditCardName = "";

  //    public int creditCardExpiryMonth;
  public String creditCardExpiryMonth = "";

  //    public int creditCardExpiryYear;
  public String creditCardExpiryYear = "";

  //    public boolean smoking;
  public String smoking = "";

  //    public int beds;
  public String beds = "";

  public Booking() {
  }

  public Booking(Hotel hotel, User user) {
    this.hotel = hotel;
    this.user = user;
  }

  public BigDecimal getTotal() {
    return hotel.price.multiply(new BigDecimal(getNights()));
  }

  public int getNights() {
    // return (int) ( checkoutDate.getTime() - checkinDate.getTime() ) / 1000 / 60 / 60 / 24;
    // todo
    return 2;
  }

  public String getDescription() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    return hotel == null ? null : hotel.name +
      ", " + df.format(checkinDate) +
      " to " + df.format(checkoutDate);
  }

  public String toString() {
    return "Booking(" + user + "," + hotel + ")";
  }

  static {

  }

  private static final AtomicInteger sequence = new AtomicInteger();
  private static final Map<String, Booking> bookings = new LinkedHashMap<String, Booking>();

  public void create() {
    id = "" + sequence;
    bookings.put(id, this);
  }

  public void delete() {
    bookings.remove(id);
    id = null;
  }

  public static Booking find(String id) {
    return bookings.get(id);
  }

  public static List<Booking> findByUser(String username) {
    ArrayList<Booking> list = new ArrayList<Booking>();
    for (Booking booking : bookings.values()) {
      if (booking.user.username.equals(username)) {
        list.add(booking);
      }
    }
    return list;
  }
}
