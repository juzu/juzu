/*
 * Copyright 2014 eXo Platform SAS
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
  
package org.juzu.tutorial.juzcret.step7;

import juzu.*;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.plugin.ajax.Ajax;
import juzu.plugin.validation.ValidationError;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;
import juzu.request.SecurityContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.juzu.tutorial.juzcret.step7.models.Comment;
import org.juzu.tutorial.juzcret.step7.services.SecretService;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

public class JuZcretApplication implements RequestLifeCycle {

  @Inject
  PortletPreferences prefs;
  
  @Inject
  SecretService secretService;

  @Inject
  @Path("secretWall.gtmpl")
  org.juzu.tutorial.juzcret.step7.templates.secretWall secretWall;
  
  @Inject
  @Path("addSecret.gtmpl")
  org.juzu.tutorial.juzcret.step7.templates.addSecret addSecret;
  
  @Inject
  @Path("editMode.gtmpl")
  org.juzu.tutorial.juzcret.step7.templates.editMode editMode;
  
  public static final String ENABLE_COMMENT = "enableComment";

  private static final String ANONYMOUS = "Anonymous";

  @View
  public Response.Content index(RequestContext context) {
    boolean enableComment = Boolean.parseBoolean(prefs.getValue(ENABLE_COMMENT, "true"));
    if (PortletMode.EDIT.equals(context.getProperty(JuzuPortlet.PORTLET_MODE))) {
      return editMode.with().enableComment(enableComment).ok();
    } else {
      return secretWall.with().enableComment(enableComment).secretsList(secretService.getSecrets()).ok();        
    }
   }
  
  @View
  public Response.Content addSecretForm() {
      return addSecret.ok();
   }
  
  @Ajax
  @Resource
  public Response addComment(String secretId, @Mapped @Valid Comment comment, SecurityContext context) {    
    comment.setUserId(getCurrentUser(context));
    Comment result = secretService.addComment(secretId, comment);
    if (result != null) {
      return Response.ok(new JSONObject(result).toString()).withMimeType("text/json");
    } else {
      return Response.status(503);
    }
  }

  @Ajax
  @Resource
  public Response addLike(String secretId, SecurityContext context) {    
    Set<String> likes = secretService.addLike(secretId, getCurrentUser(context));
    if (likes != null) {
      return Response.ok(new JSONArray(likes).toString()).withMimeType("text/json");
    } else {
      return Response.status(503);
    }
  }
  
  @Action
  public Response.View addSecret(String msg, String imgURL) {
      secretService.addSecret(msg, imgURL);
      return JuZcretApplication_.index();
  }
  
  @Action
  public Response.View enableComment(String enableComment) throws ReadOnlyException, ValidatorException, IOException {
    if ("on".equals(enableComment)) {
      enableComment = "true";
    }
    prefs.setValue(ENABLE_COMMENT, enableComment);
    prefs.store();
    return JuZcretApplication_.index().with(JuzuPortlet.PORTLET_MODE, PortletMode.VIEW);
  }
  
  @Override
  public void endRequest(RequestContext context) {
    Response response = context.getResponse();
    if (response instanceof ValidationError) {
      ValidationError error = (ValidationError)response;
      Set<ConstraintViolation<Object>> violations = error.getViolations();
      
      String msg = violations.iterator().next().getMessage();
      response = Response.ok(msg).withMimeType("text/html");
      context.setResponse(response);
    }
  }

  @Override
  public void beginRequest(RequestContext context) {    
  }
  
  private String getCurrentUser(SecurityContext context) {
    Principal user = context.getUserPrincipal();
    if (user == null) {
      return ANONYMOUS;
    } else {      
      return user.getName();          
    }
  }
}
