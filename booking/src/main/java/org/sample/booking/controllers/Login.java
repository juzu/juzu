package org.sample.booking.controllers;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Named("login")
@SessionScoped
public class Login implements Serializable
{

   /** . */
   private String userName;

   public boolean isConnected()
   {
      return userName != null;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }
}
