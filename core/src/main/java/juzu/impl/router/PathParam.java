/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package juzu.impl.router;

import juzu.impl.router.metadata.PathParamDescriptor;
import juzu.impl.router.regexp.RENode;
import juzu.impl.router.regexp.REParser;
import juzu.impl.router.regexp.RERenderer;
import juzu.impl.router.regexp.REVisitor;
import juzu.impl.router.regexp.SyntaxException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PathParam extends Param {

  static PathParam create(QualifiedName name, Router router) {
    return create(new PathParamDescriptor(name), router);
  }

  static PathParam create(PathParamDescriptor descriptor, Router router) {
    if (descriptor == null) {
      throw new NullPointerException("No null descriptor accepted");
    }

    //
    String regex = null;
    EncodingMode encodingMode = EncodingMode.FORM;
    if (descriptor != null) {
      regex = descriptor.getPattern();
      encodingMode = descriptor.getEncodingMode();
    }

    //
    if (regex == null) {
      if (encodingMode == EncodingMode.FORM) {
        regex = ".+";
      }
      else {
        regex = "[^/]+";
      }
    }

    // Now work on the regex
    StringBuilder routingRegex = new StringBuilder();
    Regex[] renderingRegexes;
    String[] templatePrefixes;
    String[] templateSuffixes;
    try {
      REVisitor<RuntimeException> transformer = descriptor.getCaptureGroup() ?
          new CaptureGroupTransformation() : new NonCaptureGroupTransformation();
      REParser parser = new REParser(regex);

      //
      RENode.Disjunction routingDisjunction = parser.parseDisjunction();
      if (encodingMode == EncodingMode.FORM) {
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
      renderingRegexes = new Regex[alt.size()];
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
        descriptor.getQualifiedName(),
        encodingMode,
        routingRegex.toString(),
        renderingRegexes,
        templatePrefixes,
        templateSuffixes);
  }

  /** . */
  final EncodingMode encodingMode;

  /** . */
  final String routingRegex;

  /** . */
  final Regex[] matchingRegex;

  /** . */
  final String[] templatePrefixes;

  /** . */
  final String[] templateSuffixes;

  private PathParam(
      QualifiedName name,
      EncodingMode encodingMode,
      String routingRegex,
      Regex[] matchingRegex,
      String[] templatePrefixes,
      String[] templateSuffixes) {
    super(name);

    //
    if (matchingRegex == null || matchingRegex.length == 0) {
      throw new NullPointerException("No null or empty pattern accepted");
    }

    //
    this.encodingMode = encodingMode;
    this.routingRegex = routingRegex;
    this.matchingRegex = matchingRegex;
    this.templatePrefixes = templatePrefixes;
    this.templateSuffixes = templateSuffixes;
  }

  @Override
  public String toString() {
    return "PathParam[name=" + name + ",encodingMode=" + encodingMode + ",pattern=" + matchingRegex + "]";
  }
}
