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

import javax.inject.Named;

import java.util.HashMap;
import java.util.Map;

import juzu.FlashScoped;

@Named("violation")
@FlashScoped
public class Violation {
  public static String CHECKIN_DATE = "checkinDate";
  public static String CHECKOUT_DATE = "checkoutDate";
  public static String CREDIT_CARD = "creditCard";
  public static String CREDIT_CARD_NAME = "creditCardName";
  public static String REGISTER = "register";
  
  private Map<String, String> violations = new HashMap<String, String>();
  
  public void add(String propName, String errMsg) {
    violations.put(propName, errMsg);
  }
  
  public String get(String propName) {
    return violations.get(propName);
  }
}