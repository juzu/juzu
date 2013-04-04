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
