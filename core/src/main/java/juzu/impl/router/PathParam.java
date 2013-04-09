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

package juzu.impl.router;

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.impl.router.regex.RERenderer;
import juzu.impl.router.regex.REVisitor;
import juzu.impl.router.regex.SyntaxException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PathParam {

  /** . */
  final String name;

  /** . */
  final boolean preservePath;

  /** . */
  final String routingRegex;

  /** . */
  final RERef[] matchingRegex;

  /** . */
  final String[] templatePrefixes;

  /** . */
  final String[] templateSuffixes;

  private PathParam(
      String name,
      boolean preservePath,
      String routingRegex,
      RERef[] matchingRegex,
      String[] templatePrefixes,
      String[] templateSuffixes) {

    //
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    if (matchingRegex == null || matchingRegex.length == 0) {
      throw new NullPointerException("No null or empty pattern accepted");
    }

    //
    this.name = name;
    this.preservePath = preservePath;
    this.routingRegex = routingRegex;
    this.matchingRegex = matchingRegex;
    this.templatePrefixes = templatePrefixes;
    this.templateSuffixes = templateSuffixes;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "PathParam[name=" + name + ",preservePath=" + preservePath + ",pattern=" + Arrays.asList(matchingRegex) + "]";
  }

  /**
   * Returns a builder matching the the specified regex.
   *
   * @param regex the regex to match
   * @return the builder
   */
  public static Builder matching(String regex) {
    return new Builder().matching(regex);
  }

  /**
   * Returns a builder matching any expression.
   *
   * @return the builder
   */
  public static Builder matchingAny() {
    return new Builder();
  }

  public static class Builder {

    /** . */
    private String pattern;

    /** . */
    private boolean preservePath;

    /** . */
    private boolean captureGroup;

    public Builder() {
      this.preservePath = false;
      this.captureGroup = false;
    }

    PathParam build(Router router, String name) {

      Builder descriptor = this;

      //
      if (descriptor == null) {
        throw new NullPointerException("No null descriptor accepted");
      }

      //
      String regex;
      boolean preservePath = false;
      if (descriptor != null) {
        regex = descriptor.getPattern();
        preservePath = descriptor.preservePath;
      } else {
        regex = null;
      }

      //
      if (regex == null) {
        regex = preservePath ? "[^/]+" : ".+";
      }

      // Now work on the regex
      StringBuilder routingRegex = new StringBuilder();
      RERef[] renderingRegexes;
      String[] templatePrefixes;
      String[] templateSuffixes;
      try {
        REVisitor<RuntimeException> transformer = descriptor.getCaptureGroup() ?
            new CaptureGroupTransformation() : new NonCaptureGroupTransformation();
        REParser parser = new REParser(regex);

        //
        RENode.Disjunction routingDisjunction = parser.parseDisjunction();
        if (!preservePath) {
          CharEscapeTransformation escaper = new CharEscapeTransformation('/', '_');
          routingDisjunction.accept(escaper);
        }
        routingDisjunction.accept(transformer);
        RERenderer.render(routingDisjunction, routingRegex);

        //
        parser.reset();
        RENode.Disjunction renderingDisjunction = parser.parseDisjunction();
        ValueResolverFactory factory = new ValueResolverFactory();
        renderingDisjunction.accept(transformer);
        List<ValueResolverFactory.Alternative> alt = factory.foo(renderingDisjunction);
        renderingRegexes = new RERef[alt.size()];
        templatePrefixes = new String[alt.size()];
        templateSuffixes = new String[alt.size()];
        for (int i = 0;i < alt.size();i++) {
          ValueResolverFactory.Alternative v = alt.get(i);
          StringBuilder valueMatcher = v.getValueMatcher();
          valueMatcher.insert(0, '^');
          valueMatcher.append("$");
          renderingRegexes[i] = router.compile(valueMatcher.toString());
          templatePrefixes[i] = v.getPrefix();
          templateSuffixes[i] = v.getSuffix();
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      catch (SyntaxException e) {
        throw new RuntimeException(e);
      }
      catch (MalformedRouteException e) {
        throw new RuntimeException(e);
      }

      //
      return new PathParam(
          name,
          preservePath,
          routingRegex.toString(),
          renderingRegexes,
          templatePrefixes,
          templateSuffixes);
    }

    public Builder matching(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder captureGroup(boolean capture) {
      this.captureGroup = capture;
      return this;
    }

    public Builder preservePath(boolean preservePath) {
      this.preservePath = preservePath;
      return this;
    }

    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }

    public boolean getCaptureGroup() {
      return captureGroup;
    }

    public void setCaptureGroup(boolean captureGroup) {
      this.captureGroup = captureGroup;
    }

    public boolean getPreservePath() {
      return preservePath;
    }

    public void setPreservePath(boolean preservePath) {
      this.preservePath = preservePath;
    }
  }
}
