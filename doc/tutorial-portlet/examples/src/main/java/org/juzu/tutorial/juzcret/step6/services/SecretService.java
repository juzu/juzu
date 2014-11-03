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

package org.juzu.tutorial.juzcret.step6.services;

import org.juzu.tutorial.juzcret.step6.models.Comment;
import org.juzu.tutorial.juzcret.step6.models.Secret;

import java.util.List;
import java.util.Set;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 9/6/14
 */
public interface SecretService {

  public List<Secret> getSecrets();

  public void addSecret(String message, String imageUrl);  
  
  public Comment addComment(String secretId, Comment comment);
  
  public Set<String> addLike(String secretId, String userId);
}
