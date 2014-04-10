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

import juzu.Mapped;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
// @Table(name="Customer")
@Mapped
public class User {

  @NotNull
  @Size(min = 4, max = 20)
  @Pattern(regexp = "^\\w*$", message = "Not a valid username")
  public String username;

  @NotNull
  @Size(min = 5, max = 20)
  public String password;

  @NotNull
  @Size(max = 100)
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