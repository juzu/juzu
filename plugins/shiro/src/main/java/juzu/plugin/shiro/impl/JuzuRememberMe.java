/*
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
package juzu.plugin.shiro.impl;

import javax.servlet.http.Cookie;

import juzu.impl.request.Request;
import juzu.request.HttpContext;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class JuzuRememberMe extends AbstractRememberMeManager {

  /**
   * The default name of the underlying rememberMe cookie which is
   * {@code rememberMe}.
   */
  private final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "rememberMe";

  /**
   * The value of deleted cookie (with the maxAge 0).
   */
  private final String DELETED_COOKIE_VALUE = "deleteMe";

  @Override
  protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext) {
    String base64 = readCookieValue(DEFAULT_REMEMBER_ME_COOKIE_NAME);
    if (DELETED_COOKIE_VALUE.equals(base64)) {
      return null;
    }

    if (base64 != null) {
      base64 = ensurePadding(base64);
      return Base64.decode(base64);
    }
    return null;
  }

  /**
   * Sometimes a user agent will send the rememberMe cookie value without
   * padding, most likely because {@code =} is a separator in the cookie header.
   * <p/>
   * Contributed by Luis Arias. Thanks Luis!
   * 
   * @param base64
   *          the base64 encoded String that may need to be padded
   * @return the base64 String padded if necessary.
   */
  private String ensurePadding(String base64) {
    int length = base64.length();
    if (length % 4 != 0) {
      StringBuilder sb = new StringBuilder(base64);
      for (int i = 0; i < length % 4; ++i) {
        sb.append('=');
      }
      base64 = sb.toString();
    }
    return base64;
  }

  private String readCookieValue(String name) {
    HttpContext context = Request.getCurrent().getBridge().getHttpContext();
    Cookie[] cookies = context.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public void forgetIdentity(SubjectContext subjectContext) {
  }

  @Override
  protected void forgetIdentity(Subject subject) {
  }

  @Override
  protected void rememberSerializedIdentity(Subject subject, byte[] serialized) {
  }
}
