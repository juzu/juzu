package org.sample.booking.models;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
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
    public String checkinDate;

//    @Required
//     @Temporal(TemporalType.DATE)
//    public Date checkoutDate;
    public String checkoutDate;

//    @Required(message="Credit card number is required")
//    @Match(value="^\\d{16}$", message="Credit card number must be numeric and 16 digits long")
    public String creditCard;

//    @Required(message="Credit card name is required")
//    @MinSize(value=3, message="Credit card name is required")
//    @MaxSize(value=70, message="Credit card name is required")
    public String creditCardName;
    public int creditCardExpiryMonth;
    public int creditCardExpiryYear;
    public boolean smoking;
    public int beds;

    public Booking(Hotel hotel, User user) {
        this.hotel = hotel;
        this.user = user;
    }

    public BigDecimal getTotal() {
        return hotel.price.multiply( new BigDecimal( getNights() ) );
    }

    public int getNights() {
       // return (int) ( checkoutDate.getTime() - checkinDate.getTime() ) / 1000 / 60 / 60 / 24;
       // todo
       return 2;
    }

    public String getDescription() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return hotel==null ? null : hotel.name +
            ", " + df.format( checkinDate ) +
            " to " + df.format( checkoutDate );
    }

    public String toString() {
        return "Booking(" + user + ","+ hotel + ")";
    }

   static
   {

   }

   private static final AtomicInteger sequence = new AtomicInteger();
   private static final Map<String, Booking> bookings = new LinkedHashMap<String, Booking> ();

   public void create()
   {
      id = "" + sequence;
      bookings.put(id, this);
   }

   public void delete()
   {
      bookings.remove(id);
      id = null;
   }

   public static Booking find(String id)
   {
      return bookings.get(id);
   }

   public static List<Booking> findByUser(String username)
   {
      ArrayList<Booking> list = new ArrayList<Booking>();
      for (Booking booking : bookings.values())
      {
         if (booking.user.username.equals(username))
         {
            list.add(booking);
         }
      }
      return list;
   }
}
