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

package org.juzu.tutorial.juzcret.step2;

import javax.inject.Inject;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;

import org.juzu.tutorial.juzcret.step2.services.SecretService;

public class JuZcretApplication {

  @Inject
  SecretService                                        secretService;

  @Inject
  @Path("secretWall.gtmpl")
  org.juzu.tutorial.juzcret.step2.templates.secretWall secretWall;

  @Inject
  @Path("addSecret.gtmpl")
  org.juzu.tutorial.juzcret.step2.templates.addSecret  addSecret;

  @View
  public Response.Content index() {
    return secretWall.with().secretsList(secretService.getSecrets()).ok();
  }

  @View
  public Response.Content addSecretForm() {
    return addSecret.ok();
  }

  @Action
  public Response.View addSecret(String msg, String imgURL) {
    secretService.addSecret(msg, imgURL);
    return JuZcretApplication_.index();
  }
}
