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
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.sample.booking.Flash;
import org.sample.booking.models.User;

import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Application {

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
  @Path("index.gtmpl")
  Template index;

  @Inject
  @Path("register.gtmpl")
  Template register;

  @Inject
  Login login;

  @Inject
  Hotels hotels;

  @Inject
  Flash flash;

  @View
  @Route("/")
  public void index() {
    if (login.isConnected()) {
      hotels.index();
    }
    else {
      index.render();
    }
  }

  @View
  @Route("/register")
  public void register() {
    register.render();
  }

  @Action
  @Route("/register")
  public Response saveUser(User user, String verifyPassword) {
/*
       validation.required(verifyPassword);
       validation.equals(verifyPassword, user.password).message("Your password doesn't match");
       if(validation.hasErrors()) {
           render("@register", user, verifyPassword);
       }
*/
    User.create(user);
    login.setUserName(user.username);
    flash.setSuccess("Welcome, " + user.name);
    return Application_.index();
  }


  @Action
  @Route("/login")
  public Response login(User u) {
    System.out.println("Want login " + u.username + " " + u.password);
    User user = User.find(u.username, u.password);
    if (user != null) {
      login.setUserName(user.username);
      flash.setSuccess("Welcome, " + user.name);
      return Application_.index();
    }
    else {
      // Oops
      flash.setUsername(u.username);
      flash.setError("Login failed");
      return null;
    }
  }

  @Action
  @Route("/logout")
  public Response logout() {
    login.setUserName(null);
    return Application_.index();
  }
}
