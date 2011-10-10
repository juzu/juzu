package org.sample.booking.controllers;

import org.juzu.Action;
import org.juzu.Path;
import org.juzu.Render;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.inject.Inject;
import java.io.IOException;

import org.sample.booking.*;
import org.sample.booking.models.User;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Application
{

/*
   @Before
   static void addUser() {
       User user = connected();
       if(user != null) {
           renderArgs.put("user", user);
       }
   }

   static User connected() {
       if(renderArgs.get("user") != null) {
           return renderArgs.get("user", User.class);
       }
       String username = session.get("user");
       if(username != null) {
           return User.find("byUsername", username).first();
       }
       return null;
   }

   // ~~
   */

   @Inject
   Printer printer;

   @Inject @Path("index.gtmpl")
   Template index;

   @Inject @Path("register.gtmpl")
   Template register;

   @Inject
   Login login;

   @Inject
   Hotels hotels;

   @Inject
   Flash flash;

   @Render
   public void index() throws IOException
   {
      if (login.isConnected())
      {
         hotels.index();
      }
      else
      {
         index.render(printer);
      }
   }

   @Render
   public void register() throws IOException
   {
      register.render(printer);
   }

   @Action
   public void saveUser(String username, String name, String password, String verifyPassword)
   {
/*
       validation.required(verifyPassword);
       validation.equals(verifyPassword, user.password).message("Your password doesn't match");
       if(validation.hasErrors()) {
           render("@register", user, verifyPassword);
       }
*/
      User user = new User(name, password, verifyPassword);
      user.username = username;
      User.create(user);
      login.setUserName(user.username);
      flash.setSuccess("Welcome, " + user.name);
      Application_.index();
   }


   @Action
   public void login(String username, String password)
   {
      System.out.println("Want login " + username + " " + password);
      User user = User.find(username, password);
      if (user != null)
      {
         login.setUserName(user.name);
         flash.setSuccess("Welcome, " + user.name);
         Hotels_.index();
      }
      else
      {
        // Oops
        flash.setUsername(username);
        flash.setError("Login failed");
      }
   }

   @Action
   public void logout()
   {
      login.setUserName(null);
      Application_.index();
   }
}
