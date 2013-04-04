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
public class JRegexFactory extends REFactory {

  /** . */
  public static final REFactory INSTANCE = new JRegexFactory();

  private JRegexFactory() {
  }

  @Override
  public RE compile(String pattern) {
    return new JRegex(pattern);
  }

  @Override
  public String getName() {
    return "jregex";
  }

  public static class JRegex extends RE {

    /** . */
    private final jregex.Pattern pattern;

    public JRegex(String regex) {
      this.pattern = new jregex.Pattern(regex);
    }

    public Matcher matcher() {
      return new Matcher() {

        /** . */
        private jregex.Matcher impl;

        private jregex.Matcher get(CharSequence seq) {
          String s = seq.toString();
          if (impl == null) {
            impl = pattern.matcher(s);
          }
          else {
            impl.setTarget(s);
          }
          return impl;
        }

        @Override
        public boolean matches(CharSequence s) {
          return get(s).matches();
        }

        @Override
        public Match[] find(CharSequence s) {
          jregex.Matcher matcher = get(s);
          if (matcher.find()) {
            Match[] matches = new Match[matcher.groupCount()];
            for (int i = 0;i < matcher.groupCount();i++) {
              if (matcher.isCaptured(i)) {
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
      return pattern.toString();
    }
  }
}
