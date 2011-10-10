package org.sample.booking;

import org.juzu.FlashScoped;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Named("flash")
@FlashScoped
public class Flash
{

   private String success = "";
   private String error = "";
   private String username = "";

   public String getSuccess()
   {
      return success;
   }

   public void setSuccess(String success)
   {
      this.success = success;
   }

   public String getError()
   {
      return error;
   }

   public void setError(String error)
   {
      this.error = error;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }
}
