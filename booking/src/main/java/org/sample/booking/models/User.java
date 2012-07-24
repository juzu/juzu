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

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
// @Table(name="Customer")
@Param
public class User {

  //    @Required
//    @MaxSize(15)
//    @MinSize(4)
//    @Match(value="^\\w*$", message="Not a valid username")
  public String username;

  //    @Required
//    @MaxSize(15)
//    @MinSize(5)
  public String password;

  //    @Required
//    @MaxSize(100)
  public String name;

  public User() {
  }

  public User(String name, String password, String username) {
    this.name = name;
    this.password = password;
    this.username = username;
  }

  public String toString() {
    return "User(" + username + ")";
  }

  /** . */
  private static final Map<String, User> users = new HashMap<String, User>();

  static {
    create(new User("Demo User", "demo", "demo"));
  }

  public static User find(String username, String password) {
    return users.get(username);
  }

  public static void create(User user) {
    users.put(user.username, user);
  }
}