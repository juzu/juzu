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
package juzu.plugin.shiro.impl.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import juzu.impl.bridge.spi.servlet.ServletWebBridge;
import juzu.impl.request.Request;
import juzu.request.HttpContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;
import org.apache.shiro.io.DefaultSerializer;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class RememberMeUtil {

  /**
   * The default name of the underlying rememberMe cookie which is
   * {@code rememberMe}.
   */
  private final static String DEFAULT_REMEMBER_ME_COOKIE_NAME = "rememberMe";

  /**
   * The value of deleted cookie (with the maxAge 0).
   */
  private final static String DELETED_COOKIE_VALUE = "deleteMe";

  /**
   * The number of seconds in one year (= 60 * 60 * 24 * 365).
   */
  private final static int ONE_YEAR = 60 * 60 * 24 * 365;

  /** . */
  private final static long DAY_MILLIS = 86400000; // 1 day = 86,400,000
                                                   // milliseconds

  /** . */
  private final static String GMT_TIME_ZONE_ID = "GMT";

  /** . */
  private final static String COOKIE_DATE_FORMAT_STRING = "EEE, dd-MMM-yyyy HH:mm:ss z";

  /** . */
  private final static String NAME_VALUE_DELIMITER = "=";

  /** . */
  private final static String ATTRIBUTE_DELIMITER = "; ";

  /** . */
  private final static String COOKIE_HEADER_NAME = "Set-Cookie";

  /** . */
  private final static String PATH_ATTRIBUTE_NAME = "Path";

  /** . */
  private final static String EXPIRES_ATTRIBUTE_NAME = "Expires";

  /** . */
  private final static String MAXAGE_ATTRIBUTE_NAME = "Max-Age";

  /** . */
  private final static String DOMAIN_ATTRIBUTE_NAME = "Domain";

  public static void rememberSerialized() {
    HttpContext context = Request.getCurrent().getHttpContext();
    if (context instanceof ServletWebBridge) {
      ServletWebBridge bridge = (ServletWebBridge) context;
  
      // base 64 encode it and store as a cookie:
      DefaultSerializer<PrincipalCollection> serializer = new DefaultSerializer<PrincipalCollection>();
      byte[] serialized = serializer.serialize(SecurityUtils.getSubject().getPrincipals());
      serialized = encrypt(serialized);
      String base64 = Base64.encodeToString(serialized);

      String name = DEFAULT_REMEMBER_ME_COOKIE_NAME;
      String value = base64;
      String domain = context.getServerName();
      String path = context.getContextPath();
      int maxAge = ONE_YEAR; // always zero for deletion
      final String headerValue = buildHeaderValue(name, value, domain.trim(), path.trim(), maxAge);
      bridge.getResponse().setHeader(COOKIE_HEADER_NAME, headerValue);
    }
  }

  public static void forgetIdentity() {
    HttpContext context = Request.getCurrent().getHttpContext();
    if (context instanceof ServletWebBridge) {
      ServletWebBridge bridge = (ServletWebBridge) context;
      
      String name = DEFAULT_REMEMBER_ME_COOKIE_NAME;
      String value = DELETED_COOKIE_VALUE;
      String domain = context.getServerName();
      String path = context.getContextPath();
      int maxAge = 0; // always zero for deletion
      final String headerValue = buildHeaderValue(name, value, domain.trim(), path.trim(), maxAge);
      bridge.getResponse().setHeader(COOKIE_HEADER_NAME, headerValue);
    }
  }
  
  private static String buildHeaderValue(String name, String value, String domain, String path, int maxAge) {
    StringBuilder sb = new StringBuilder(name).append(NAME_VALUE_DELIMITER);
    if (value != null && !value.isEmpty()) {
      sb.append(value);
    }
    if (domain != null && !domain.isEmpty()) {
      sb.append(ATTRIBUTE_DELIMITER);
      sb.append(DOMAIN_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(domain);
    }
    if (path != null && !path.isEmpty()) {
      sb.append(ATTRIBUTE_DELIMITER);
      sb.append(PATH_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(path);
    }

    if (maxAge >= 0) {
      sb.append(ATTRIBUTE_DELIMITER);
      sb.append(MAXAGE_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(maxAge);
      sb.append(ATTRIBUTE_DELIMITER);
      Date expires;
      if (maxAge == 0) {
        // delete the cookie by specifying a time in the past (1 day ago):
        expires = new Date(System.currentTimeMillis() - DAY_MILLIS);
      } else {
        // Value is in seconds. So take 'now' and add that many seconds, and
        // that's our expiration date:
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, maxAge);
        expires = cal.getTime();
      }
      String formatted = toCookieDate(expires);
      sb.append(EXPIRES_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(formatted);
    }
    return sb.toString();
  }

  /**
   * Formats a date into a cookie date compatible string (Netscape's
   * specification).
   * 
   * @param date
   *          the date to format
   * @return an HTTP 1.0/1.1 Cookie compatible date string (GMT-based).
   */
  private static String toCookieDate(Date date) {
    TimeZone tz = TimeZone.getTimeZone(GMT_TIME_ZONE_ID);
    DateFormat fmt = new SimpleDateFormat(COOKIE_DATE_FORMAT_STRING, Locale.US);
    fmt.setTimeZone(tz);
    return fmt.format(date);
  }

  private static byte[] encrypt(byte[] serialized) {
    byte[] value = serialized;
    CipherService cipherService = new AesCipherService();
    if (cipherService != null) {
      ByteSource byteSource = cipherService.encrypt(serialized, Base64.decode("kPH+bIxk5D2deZiIxcaaaA=="));
      value = byteSource.getBytes();
    }
    return value;
  }
}
