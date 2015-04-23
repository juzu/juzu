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

package org.juzu.tutorial.juzcret.step3.services;

import org.juzu.tutorial.juzcret.step3.models.Secret;

import javax.inject.Singleton;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/6/14
 */
@Singleton
public class SecretServiceMemImpl implements SecretService {

  private List<Secret> secretsList;

  public List<Secret> getSecrets() {
    if (secretsList == null) {
      secretsList = new LinkedList<Secret>();
      addFakeSecrets();
    }
    return secretsList;
//    return Collections.emptyList();
   }

  public void addSecret(String message, String imageUrl) {
       Secret secret = new Secret();
       secret.setMessage(message);
       secret.setImageURL(imageUrl);
       secret.setCreatedDate(new Date());
       secretsList.add(secret);
   }

  private void addFakeSecrets() {
     addSecret("Yesterday I said I missed my PL meeting because I have to many work. In fact I was drinking free beer in Barbetta pub",
              "https://c1.staticflickr.com/3/2385/2345543856_6d0fbafb66_z.jpg?zz=1");
     addSecret("I have a master degree but I still use Google to calculate 3*8",
              "https://yy2.staticflickr.com/7244/7245177220_3f17ee9fb8_z.jpg");
     addSecret("I am in relationship for 2 years. He is awesome, powerful and I never go out without him. His name is Linux",
              "http://fc02.deviantart.net/fs71/f/2009/364/9/d/christmas_love_by_skubaNiec.jpg");
     addSecret("I spent 2 hours a day to train my cat to perform a backflip",
              "http://fc06.deviantart.net/fs15/i/2007/008/e/b/colour_cat_wallpaper_by_jellyplant.jpg");
     addSecret("I pretend to be a spy when I go out. In reality my job is to perform photocopy at the embassy",
              "https://c2.staticflickr.com/2/1230/5108154392_3cc02cac67_z.jpg");
   }
 }