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

package org.sample.booking.models;

import juzu.Format;
import juzu.Mapped;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
@Mapped
public class Booking {

  public String id;

//     @ManyToOne
  public User user;

//     @ManyToOne
  public Hotel hotel;

//     @Temporal(TemporalType.DATE)
  @NotNull
  @Format("yyyy-MM-dd")
  public Date checkinDate;

//     @Temporal(TemporalType.DATE)
  @NotNull
  @Format("yyyy-MM-dd")
  public Date checkoutDate;

  @NotNull(message = "Credit card number is required")
  @Pattern(regexp = "^\\d{16}$", message = "Credit card number must be numeric and 16 digits long")
  public String creditCard = "";

  @Size(min = 3, max = 70, message = "Credit card name must be between 3 and 70 letters long")
  @NotNull(message = "Credit card name is required")
  public String creditCardName;

  public int creditCardExpiryMonth;

  public int creditCardExpiryYear;

  public boolean smoking;

  public int beds;

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
