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
  public Response.Content index() {
    if (login.isConnected()) {
      return hotels.index();
    }
    else {
      return index.ok();
    }
  }

  @View
  @Route("/register")
  public Response.Content register() {
    return register.ok();
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
