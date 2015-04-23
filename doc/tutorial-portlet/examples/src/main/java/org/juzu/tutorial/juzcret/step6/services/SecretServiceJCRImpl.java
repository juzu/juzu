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

package org.juzu.tutorial.juzcret.step6.services;

import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.juzu.tutorial.juzcret.step6.models.Comment;
import org.juzu.tutorial.juzcret.step6.models.Secret;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

@Singleton
public class SecretServiceJCRImpl implements SecretService {

  private static final String     SECRET_APP = "SecretApplication";

  private static final String CREATED_DATE = "exo:createdDate";

  private static final String ID = "exo:id";

  private static final String IMAGE_URL = "exo:imageURL";

  private static final String LIKES = "exo:likes";

  private static final String MESSAGE = "exo:message";

  private static final String CONTENT = "exo:content";

  private static final String USER_ID = "exo:userId";

  private static final String SECRET_NODE_TYPE = "exo:secret";

  private static final String COMMENT_NODE_TYPE = "exo:secretComment";

  @Inject
  private SessionProviderService  sessionService;

  @Inject
  private NodeHierarchyCreator    nodeCreator;
  
  @PostConstruct
  public void initialize() {
    if (getSecrets().size() == 0) {
      addFakeSecrets();
    }
  }

  @Override
  public List<Secret> getSecrets() {        
    List<Secret> secrets = new LinkedList<Secret>();
    try {
      Node secretHome = getSecretHome();
      NodeIterator iterChild = secretHome.getNodes();
      while (iterChild.hasNext()) {
        secrets.add(buildSecret(iterChild.nextNode()));
      }
      return secrets;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void addSecret(String message, String imageUrl) {
    String id = UUID.randomUUID().toString();

    try {
      Node secretHome = getSecretHome();
      Node secret = secretHome.addNode(id, SECRET_NODE_TYPE);
      secret.setProperty(ID, id);
      secret.setProperty(MESSAGE, message);
      secret.setProperty(IMAGE_URL, imageUrl);
      secret.setProperty(CREATED_DATE, Calendar.getInstance());
      secret.getSession().save();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Comment addComment(String secretId, Comment comment) {
    String id = UUID.randomUUID().toString();
    
    try {
      Node secret = getSecretNode(secretId);
      
      if (secret != null) {
        Node cNode = secret.addNode(id, COMMENT_NODE_TYPE);
        cNode.setProperty(ID, id);
        cNode.setProperty(USER_ID, comment.getUserId());
        cNode.setProperty(CONTENT, comment.getContent());
        cNode.setProperty(CREATED_DATE, Calendar.getInstance());
        
        cNode.getSession().save();
        return buildComment(cNode);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;      
  }

  @Override
  public Set<String> addLike(String secretId, String userId) {
    try {
      Node secret = getSecretNode(secretId);
      
      if (secret != null) {
        Set<String> likes = new HashSet<String>();
        if (secret.hasProperty(LIKES)) {
          Value[] values = secret.getProperty(LIKES).getValues();
          for (Value v : values) {
            likes.add(v.getString());
          }
        }
        likes.add(userId);
        secret.setProperty(LIKES, likes.toArray(new String[likes.size()]));
        
        secret.save();
        return likes;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private Node getSecretNode(String secretId) {
    try {
      Node secretHome = getSecretHome();
      Node secret = secretHome.getNode(secretId);
      return secret;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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
  
  private Secret buildSecret(Node secretNode) throws RepositoryException {
    Secret secret = new Secret();
    
    List<Comment> comments = new LinkedList<Comment>();
    NodeIterator commentIter = secretNode.getNodes();
    while (commentIter.hasNext()) {
      comments.add(buildComment(commentIter.nextNode()));
    }
    secret.setComments(comments);
    
    secret.setCreatedDate(secretNode.getProperty(CREATED_DATE).getDate().getTime());
    secret.setId(secretNode.getProperty(ID).getString());
    secret.setImageURL(secretNode.getProperty(IMAGE_URL).getString());

    Set<String> likes = new HashSet<String>();
    if (secretNode.hasProperty(LIKES)) {
      for (Value userID : secretNode.getProperty(LIKES).getValues()) {
        likes.add(userID.getString());
      }      
    }
    secret.setLikes(likes);

    secret.setMessage(secretNode.getProperty(MESSAGE).getString());
    return secret;
  }

  private Comment buildComment(Node commentNode) throws RepositoryException {
    Comment comment = new Comment();
    comment.setContent(commentNode.getProperty(CONTENT).getString());
    comment.setCreatedDate(commentNode.getProperty(CREATED_DATE).getDate().getTime());
    comment.setId(commentNode.getProperty(ID).getString());
    comment.setUserId(commentNode.getProperty(USER_ID).getString());
    return comment;
  }

  private Node getSecretHome() throws Exception {
    SessionProvider sProvider = sessionService.getSystemSessionProvider(null);
    Node publicApp = nodeCreator.getPublicApplicationNode(sProvider);
    try {
      return publicApp.getNode(SECRET_APP);
    } catch (Exception e) {
      Node secretApp = publicApp.addNode(SECRET_APP, "nt:unstructured");
      publicApp.getSession().save();
      return secretApp;
    }
  }
}
