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
package juzu.impl.router.regex;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RE {

  /** . */
  public static final Match[] NO_MATCHES = new Match[0];

  /** . */
  public static final Match NULL_MATCH = new Match(-1, -1, null);

  public abstract String getPattern();

  public abstract Matcher matcher();

  public abstract static class Matcher {

    public abstract boolean matches(CharSequence s);

    public abstract Match[] find(CharSequence s);

  }

  public static class Match {

    /** . */
    private final int start;

    /** . */
    private final int end;

    /** . */
    private final String value;

    protected Match(int start, int end, String value) {
      this.start = start;
      this.end = end;
      this.value = value;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    public String getValue() {
      return value;
    }
  }

  public static class Java extends RE {

    /** . */
    private final java.util.regex.Pattern pattern;

    public Java(String regex) {
      this.pattern = java.util.regex.Pattern.compile(regex);
    }

    public Matcher matcher() {
      return new Matcher() {

        /** . */
        private java.util.regex.Matcher impl;

        private java.util.regex.Matcher get(CharSequence s) {
          if (impl == null) {
            impl = pattern.matcher(s);
          }
          else {
            impl.reset(s);
          }
          return impl;
        }

        @Override
        public boolean matches(CharSequence s) {
          return get(s).matches();
        }

        @Override
        public Match[] find(CharSequence s) {
          java.util.regex.Matcher matcher = get(s);
          if (matcher.find()) {
            Match[] matches = new Match[1 + matcher.groupCount()];
            for (int i = 0;i <= matcher.groupCount();i++) {
              if (matcher.group() != null) {
                matches[i] = new Match(matcher.start(i), matcher.end(i), matcher.group(i));
              }
              else {
                matches[i] = NULL_MATCH;
              }
            }
            return matches;
          }
          else {
            return NO_MATCHES;
          }
        }
      };
    }

    @Override
    public String getPattern() {
      return pattern.pattern();
    }
  }
}
