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

package juzu.impl.router;

//import javanet.staxutils.IndentingXMLStreamWriter;

import juzu.impl.router.regex.RE;
import juzu.impl.router.regex.SyntaxException;
import juzu.impl.common.QualifiedName;
import juzu.impl.common.Tools;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * The implementation of the routing algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Route {

  void writeTo(XMLStreamWriter writer) throws XMLStreamException {
    if (this instanceof SegmentRoute) {
      writer.writeStartElement("segment");
      writer.writeAttribute("path", "/" + ((SegmentRoute)this).name);
      writer.writeAttribute("terminal", "" + terminal);
    }
    else if (this instanceof PatternRoute) {
      PatternRoute pr = (PatternRoute)this;
      StringBuilder path = new StringBuilder("/");
      for (int i = 0;i < pr.params.length;i++) {
        path.append(pr.chunks[i]).append("{").append(pr.params[i].name.getValue()).append("}");
      }
      path.append(pr.chunks[pr.chunks.length - 1]);
      writer.writeStartElement("pattern");
      writer.writeAttribute("path", path.toString());
      writer.writeAttribute("terminal", Boolean.toString(terminal));
      for (PathParam param : pr.params) {
        writer.writeStartElement("path-param");
        writer.writeAttribute("qname", param.name.getValue());
        writer.writeAttribute("preservePath", "" + param.preservePath);
        writer.writeAttribute("pattern", param.matchingRegex.toString());
        writer.writeEndElement();
      }
    }
    else {
      writer.writeStartElement("route");
    }

    //
    for (RouteParam routeParam : routeParamArray) {
      writer.writeStartElement("route-param");
      writer.writeAttribute("qname", routeParam.name.getValue());
      writer.writeAttribute("value", routeParam.value);
      writer.writeEndElement();
    }

    //
    for (RequestParam requestParam : requestParamArray) {
      writer.writeStartElement("request-param");
      writer.writeAttribute("qname", requestParam.name.getValue());
      writer.writeAttribute("name", requestParam.matchedName);
      if (requestParam.matchPattern != null) {
        writer.writeAttribute("value", requestParam.matchPattern.re.getPattern());
      }
      writer.writeEndElement();
    }

    //
/*
      for (Map.Entry<String, SegmentRoute[]> entry : segments.entrySet())
      {
         writer.writeStartElement("segment");
         writer.writeAttribute("name", entry.getKey());
         for (SegmentRoute segment : entry.getValue())
         {
            segment.writeTo(writer);
         }
         writer.writeEndElement();
      }

      //
      for (PatternRoute pattern : patterns)
      {
         pattern.writeTo(writer);
      }
*/

    //
    writer.writeEndElement();
  }

  @Override
  public String toString() {
    try {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      StringWriter sw = new StringWriter();
      XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(sw);
//         xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
      writeTo(xmlWriter);
      return sw.toString();
    }
    catch (XMLStreamException e) {
      throw new AssertionError(e);
    }
  }

  /** . */
  private static final Route[] EMPTY_ROUTE_ARRAY = new Route[0];

  /** . */
  private static final RouteParam[] EMPTY_ROUTE_PARAM_ARRAY = new RouteParam[0];

  /** . */
  private static final RequestParam[] EMPTY_REQUEST_PARAM_ARRAY = new RequestParam[0];

  /** . */
  private final Router router;

  /** . */
  private Route parent;

  /** . */
  private List<Route> path;

  /** . */
  private boolean terminal;

  /** . */
  private Route[] children;

  /** . */
  private Map<QualifiedName, RouteParam> routeParamMap;

  /** . */
  private RouteParam[] routeParamArray;

  /** . */
  private Map<String, RequestParam> requestParamMap;

  /** . */
  private RequestParam[] requestParamArray;

  Route(Router router) {
    this.path = Collections.singletonList(this);
    this.router = router;
    this.parent = null;
    this.terminal = true;
    this.children = EMPTY_ROUTE_ARRAY;
    this.routeParamMap = Collections.emptyMap();
    this.routeParamArray = EMPTY_ROUTE_PARAM_ARRAY;
    this.requestParamMap = Collections.emptyMap();
    this.requestParamArray = EMPTY_REQUEST_PARAM_ARRAY;
  }

  public final Route getParent() {
    return parent;
  }

  public final List<Route> getPath() {
    return path;
  }

  final boolean renderPath(RouteMatch match, URIWriter writer, boolean hasChildren) throws IOException {
    boolean endWithSlash;
    if (parent != null) {
      endWithSlash = parent.renderPath(match, writer, true);
    }
    else {
      endWithSlash = false;
    }

    //
    if (this instanceof SegmentRoute) {
      SegmentRoute sr = (SegmentRoute)this;
      if (!endWithSlash) {
        writer.append('/');
        endWithSlash = true;
      }
      String name = sr.encodedName;
      writer.append(name);
      if (name.length() > 0) {
        endWithSlash = false;
      }
    }
    else if (this instanceof PatternRoute) {
      PatternRoute pr = (PatternRoute)this;
      if (!endWithSlash) {
        writer.append('/');
        endWithSlash = true;
      }
      int i = 0;
      int count = 0;
      while (i < pr.params.length) {
        writer.append(pr.encodedChunks[i]);
        count += pr.chunks[i].length();

        //
        PathParam def = pr.params[i];
        String value = match.matched.get(def);
        count += value.length();

        // Write value
        for (int len = value.length(), j = 0;j < len;j++) {
          char c = value.charAt(j);
          if (c == router.separatorEscape) {
            if (def.preservePath) {
              writer.append('_');
            }
            else {
              writer.append('%');
              writer.append(router.separatorEscapeNible1);
              writer.append(router.separatorEscapeNible2);
            }
          }
          else if (c == '/') {
            writer.append(def.preservePath ? '/' : router.separatorEscape);
          }
          else {
            writer.appendSegment(c);
          }
        }

        //
        i++;
      }
      writer.append(pr.encodedChunks[i]);
      count += pr.chunks[i].length();
      if (count > 0) {
        endWithSlash = false;
      }
    }
    else {
      if (!hasChildren) {
        writer.append('/');
        endWithSlash = true;
      }
    }

    //
    return endWithSlash;
  }

  final void renderQueryString(RouteMatch match, URIWriter writer) throws IOException {
    if (parent != null) {
      parent.renderQueryString(match, writer);
    }

    //
    for (RequestParam requestParamDef : requestParamArray) {
      String s = match.matched.get(requestParamDef);
      if (s != null) {
        writer.appendQueryParameter(requestParamDef.matchedName, s);
      }
    }
  }

  final RouteMatch resolve(RenderContext context) {

    //
    context.enter();

    //
    RouteMatch match = null;

    //
    if (matches(context)) {

      //
      if (context.isEmpty() && terminal) {
        match = new RouteMatch(this, context);
      }

      //
      if (match == null) {
        for (Route child : children) {
          match = child.resolve(context);
          if (match != null) {
            break;
          }
        }
      }
    }

    //
    context.leave();

    //
    return match;
  }

  public final RouteMatch matches(Map<QualifiedName, String> parameters) {
    RenderContext context = new RenderContext(parameters);
    router.bilto(context);
    context.enter();
    if (_matches(context)) {
      return new RouteMatch(this, context);
    } else {
      return null;
    }
  }

  private boolean _matches(RenderContext context) {
    return (parent == null || parent._matches(context)) && matches(context);
  }

  private boolean matches(RenderContext context) {

    // Match first the static parameteters
    for (RouteParam param : routeParamArray) {
      RenderContext.Parameter entry = context.getParameter(param.name);
      if (entry != null && !entry.isMatched() && param.value.equals(entry.getValue())) {
        entry.remove(param, entry.getValue());
      }
      else {
        return false;
      }
    }

    // Match any request parameter
    for (RequestParam requestParamDef : requestParamArray) {
      RenderContext.Parameter entry = context.getParameter(requestParamDef.name);
      boolean matched = false;
      if (entry != null && !entry.isMatched()) {
        if (requestParamDef.matchPattern == null || context.matcher(requestParamDef.matchPattern).matches(entry.getValue())) {
          matched = true;
        }
      }
      if (matched) {
        entry.remove(requestParamDef, entry.getValue());
      }
      else {
        if (requestParamDef.required) {
          return false;
        }
      }
    }

    // Match any pattern parameter
    if (this instanceof PatternRoute) {
      PatternRoute prt = (PatternRoute)this;
      for (int i = 0;i < prt.params.length;i++) {
        PathParam param = prt.params[i];
        RenderContext.Parameter s = context.getParameter(param.name);
        String matched = null;
        if (s != null && !s.isMatched()) {
          for (int j = 0;j < param.matchingRegex.length;j++) {
            RERef renderingRegex = param.matchingRegex[j];
            if (context.matcher(renderingRegex).matches(s.getValue())) {
              matched = param.templatePrefixes[j] + s.getValue() + param.templateSuffixes[j];
              break;
            }
          }
        }
        if (matched != null) {
          s.remove(param, matched);
        }
        else {
          return false;
        }
      }
    }

    //
    return true;
  }

  /**
   * Create a route matcher for the a request.
   *
   * @param path          the path
   * @param requestParams the query parameters
   * @return the route matcher
   */
  public final Iterator<RouteMatch> matcher(String path, Map<String, String[]> requestParams) {
    return new RouteMatcher(this, Path.parse(path), requestParams);
  }

  static class RouteFrame {

    /** Defines the status of a frame. */
    static enum Status {
      BEGIN,

      MATCHED_PARAMS,

      PROCESS_CHILDREN,

      MATCHED,

      END

    }

    /** . */
    private final RouteFrame parent;

    /** . */
    private final Route route;

    /** . */
    private final Path path;

    /** . */
    private Status status;

    /** The matches. */
    private Map<Param, String> matches;

    /** The index when iterating child in {@link juzu.impl.router.Route.RouteFrame.Status#PROCESS_CHILDREN} status. */
    private int childIndex;

    private RouteFrame(RouteFrame parent, Route route, Path path) {
      this.parent = parent;
      this.route = route;
      this.path = path;
      this.status = Status.BEGIN;
      this.childIndex = 0;
    }

    private RouteFrame(Route route, Path path) {
      this(null, route, path);
    }

    Map<Param, String> getParameters() {
      Map<Param, String> parameters = null;
      for (RouteFrame frame = this;frame != null;frame = frame.parent) {
        if (frame.matches != null) {
          if (parameters == null) {
            parameters = new HashMap<Param, String>();
          }
          parameters.putAll(frame.matches);
        }
        for (RouteParam param : frame.route.routeParamArray) {
          if (parameters == null) {
            parameters = new HashMap<Param, String>();
          }
          parameters.put(param, param.value);
        }
      }
      return parameters != null ? parameters : Collections.<Param, String>emptyMap();
    }
  }

  static class RouteMatcher implements Iterator<RouteMatch> {

    /** . */
    private final Map<String, String[]> requestParams;

    /** . */
    private RouteFrame frame;

    /** . */
    private RouteFrame next;

    RouteMatcher(Route route, Path path, Map<String, String[]> requestParams) {
      this.frame = new RouteFrame(route, path);
      this.requestParams = requestParams;
    }

    public boolean hasNext() {
      if (next == null) {
        if (frame != null) {
          frame = route(frame, requestParams);
        }
        if (frame != null && frame.status == RouteFrame.Status.MATCHED) {
          next = frame;
        }
      }
      return next != null;
    }

    public RouteMatch next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Map<Param, String> parameters = next.getParameters();
      Route route = next.route;
      next = null;
      return new RouteMatch(route, parameters);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static RouteFrame route(RouteFrame root, Map<String, String[]> requestParams) {
    RouteFrame current = root;

    //
    if (root.status == RouteFrame.Status.MATCHED) {
      if (root.parent != null) {
        current = root.parent;
      }
      else {
        return null;
      }
    }
    else if (root.status != RouteFrame.Status.BEGIN) {
      throw new AssertionError("Unexpected status " + root.status);
    }

    //
    while (true) {
      if (current.status == RouteFrame.Status.BEGIN) {
        boolean matched = true;

        // We enter a frame
        for (RequestParam requestParamDef : current.route.requestParamArray) {
          String value = null;
          String[] values = requestParams.get(requestParamDef.matchedName);
          if (values != null && values.length > 0 && values[0] != null) {
            value = values[0];
          }
          if (value == null) {
            if (requestParamDef.required) {
              matched = false;
            }
          }
          else if (!requestParamDef.matchValue(value)) {
            matched = false;
            break;
          }
          if (value != null) {
            if (current.matches == null) {
              current.matches = new HashMap<Param, String>();
            }
            current.matches.put(requestParamDef, value);
          }
        }

        //
        if (matched) {
          // We enter next state
          current.status = RouteFrame.Status.MATCHED_PARAMS;
        }
        else {
          current.status = RouteFrame.Status.END;
        }
      }
      else if (current.status == RouteFrame.Status.MATCHED_PARAMS) {
        RouteFrame.Status next;

        // Anything that does not begin with '/' returns null
        if (current.path.length() > 0 && current.path.charAt(0) == '/') {
          // The '/' means the current controller if any, otherwise it may be processed by the pattern matching
          if (current.path.length() == 1 && current.route.terminal) {
            next = RouteFrame.Status.MATCHED;
          }
          else {
            next = RouteFrame.Status.PROCESS_CHILDREN;
          }
        }
        else {
          next = RouteFrame.Status.END;
        }

        //
        current.status = next;
      }
      else if (current.status == RouteFrame.Status.PROCESS_CHILDREN) {
        if (current.childIndex < current.route.children.length) {
          Route child = current.route.children[current.childIndex++];

          // The next frame
          RouteFrame next;

          //
          if (child instanceof SegmentRoute) {
            SegmentRoute segmentRoute = (SegmentRoute)child;

            //
            if (segmentRoute.name.length() == 0) {
              // Delegate the process to the next route
              next = new RouteFrame(current, segmentRoute, current.path);
            }
            else {
              // Find the next '/' for determining the segment and next path
              // JULIEN : this can be computed multiple times
              int pos = current.path.indexOf('/', 1);
              if (pos == -1) {
                pos = current.path.length();
              }
              String segment = current.path.getValue().substring(1, pos);

              // Determine next path
              if (segmentRoute.name.equals(segment)) {
                // Lazy create next segment path
                // JULIEN : this can be computed multiple times
                Path nextSegmentPath;
                if (pos == current.path.length()) {
                  // todo make a constant
                  nextSegmentPath = Path.SLASH;
                }
                else {
                  nextSegmentPath = current.path.subPath(pos);
                }

                // Delegate the process to the next route
                next = new RouteFrame(current, segmentRoute, nextSegmentPath);
              }
              else {
                next = null;
              }
            }
          }
          else if (child instanceof PatternRoute) {
            PatternRoute patternRoute = (PatternRoute)child;

            //
            RE.Match[] matches = patternRoute.pattern.re.matcher().find(current.path.getValue());

            // We match
            if (matches.length > 0) {
              // Build next controller context
              int nextPos = matches[0].getEnd();
              Path nextPath;
              if (current.path.length() == nextPos) {
                nextPath = Path.SLASH;
              }
              else {
                if (nextPos > 0 && current.path.charAt(nextPos - 1) == '/') {
                  nextPos--;
                }

                //
                nextPath = current.path.subPath(nextPos);
              }

              // Delegate to next patternRoute
              next = new RouteFrame(current, patternRoute, nextPath);

              // JULIEN : this can be done lazily
              // Append parameters
              int index = 1;
              for (int i = 0;i < patternRoute.params.length;i++) {
                PathParam param = patternRoute.params[i];
                for (int j = 0;j < param.matchingRegex.length;j++) {
                  RE.Match match = matches[index + j];
                  if (match.getEnd() != -1) {
                    String value;
                    if (!param.preservePath) {
                      StringBuilder sb = new StringBuilder();
                      for (int from = match.getStart();from < match.getEnd();from++) {
                        char c = current.path.charAt(from);
                        if (c == child.router.separatorEscape && current.path.getRawLength(from) == 1) {
                          c = '/';
                        }
                        sb.append(c);
                      }
                      value = sb.toString();
                    }
                    else {
                      value = match.getValue();
                    }
                    if (next.matches == null) {
                      next.matches = new HashMap<Param, String>();
                    }
                    next.matches.put(param, value);
                    break;
                  }
                  else {
                    // It can be the match of a particular disjunction
                    // or an optional parameter
                  }
                }
                index += param.matchingRegex.length;
              }
            }
            else {
              next = null;
            }
          }
          else {
            throw new AssertionError();
          }

          //
          if (next != null) {
            current = next;
          }
        }
        else {
          current.status = RouteFrame.Status.END;
        }
      }
      else if (current.status == RouteFrame.Status.MATCHED) {
        // We found a solution
        break;
      }
      else if (current.status == RouteFrame.Status.END) {
        if (current.parent != null) {
          current = current.parent;
        }
        else {
          // The end of the search
          break;
        }
      }
      else {
        throw new AssertionError();
      }
    }

    //
    return current;
  }

  final <R extends Route> R add(R route) throws MalformedRouteException {
    if (route == null) {
      throw new NullPointerException("No null route accepted");
    }
    if (route.parent != null) {
      throw new IllegalArgumentException("No route with an existing parent can be accepted");
    }

    //
    LinkedList<Param> ancestorParams = new LinkedList<Param>();
    findAncestorOrSelfParams(ancestorParams);
    LinkedList<Param> descendantParams = new LinkedList<Param>();
    for (Param param : ancestorParams) {
      route.findDescendantOrSelfParams(param.name, descendantParams);
      if (descendantParams.size() > 0) {
        throw new MalformedRouteException("Duplicate parameter " + param.name);
      }
    }

    //
    if (route instanceof PatternRoute || route instanceof SegmentRoute) {
      children = Tools.appendTo(children, route);
      terminal = false;

      // Compute path
      List<Route> path = new ArrayList<Route>(this.path.size() + 1);
      path.addAll(this.path);
      path.add(route);

      //
      route.parent = this;
      route.path = Collections.unmodifiableList(path);
    }
    else {
      throw new IllegalArgumentException("Only accept segment or pattern routes");
    }

    //
    return route;
  }

  final Set<String> getSegmentNames() {
    Set<String> names = new HashSet<String>();
    for (Route child : children) {
      if (child instanceof SegmentRoute) {
        SegmentRoute childSegment = (SegmentRoute)child;
        names.add(childSegment.name);
      }
    }
    return names;
  }

  final int getSegmentSize(String segmentName) {
    int size = 0;
    for (Route child : children) {
      if (child instanceof SegmentRoute) {
        SegmentRoute childSegment = (SegmentRoute)child;
        if (segmentName.equals(childSegment.name)) {
          size++;
        }
      }
    }
    return size;
  }

  final SegmentRoute getSegment(String segmentName, int index) {
    for (Route child : children) {
      if (child instanceof SegmentRoute) {
        SegmentRoute childSegment = (SegmentRoute)child;
        if (segmentName.equals(childSegment.name)) {
          if (index == 0) {
            return childSegment;
          }
          else {
            index--;
          }
        }
      }
    }
    return null;
  }

  final int getPatternSize() {
    int size = 0;
    for (Route route : children) {
      if (route instanceof PatternRoute) {
        size++;
      }
    }
    return size;
  }

  final PatternRoute getPattern(int index) {
    for (Route route : children) {
      if (route instanceof PatternRoute) {
        if (index == 0) {
          return (PatternRoute)route;
        }
        else {
          index--;
        }
      }
    }
    return null;
  }

  final Route add(RouteParam param) throws MalformedRouteException {
    Param existing = findParam(param.name);
    if (existing != null) {
      throw new MalformedRouteException("Duplicate parameter " + param.name);
    }
    if (routeParamArray.length == 0) {
      routeParamMap = new HashMap<QualifiedName, RouteParam>();
    }
    routeParamMap.put(param.name, param);
    routeParamArray = Tools.appendTo(routeParamArray, param);
    return this;
  }

  final Route add(RequestParam param) throws MalformedRouteException {
    Param existing = findParam(param.name);
    if (existing != null) {
      throw new MalformedRouteException("Duplicate parameter " + param.name);
    }
    if (requestParamArray.length == 0) {
      requestParamMap = new HashMap<String, RequestParam>();
    }
    requestParamMap.put(param.matchedName, param);
    requestParamArray = Tools.appendTo(requestParamArray, param);
    return this;
  }

  public Route append(String path) {
    return append(path, Collections.<QualifiedName, String>emptyMap());
  }

  public Route append(String path, Map<QualifiedName, String> params) {

    //
    class Assembler implements RouteParserHandler {
      Route current = Route.this;
      boolean path = true;
      PatternBuilder builder = new PatternBuilder();
      List<String> chunks = new ArrayList<String>();
      List<PathParam> parameterPatterns = new ArrayList<PathParam>();
      PathParam.Builder paramDesc;
      RequestParam.Builder requestParam;
      boolean lastSegment;
      public void openSegment() {
        builder.clear().expr("^/");
        chunks.clear();
        parameterPatterns.clear();
        lastSegment = false;
      }
      public void segmentChunk(CharSequence s, int from, int to) {
        builder.litteral(s, from, to);
        chunks.add(s.subSequence(from, to).toString());
        lastSegment = true;
      }
      public void closeSegment() {
        if (!lastSegment) {
          chunks.add("");
        }

        Route next;
        if (parameterPatterns.isEmpty()) {
          next = new SegmentRoute(router, chunks.get(0));
        } else {
          // We want to satisfy one of the following conditions
          // - the next char after the matched expression is '/'
          // - the expression matched until the end
          // - the match expression is the '/' expression
          builder.expr("(?:(?<=^/)|(?=/)|$)");
          next = new PatternRoute(router, router.compile(builder.build()), parameterPatterns, chunks);
        }

        //
        current = current.add(next);
      }

      public void closePath() {
        if (Route.this == current) {
          // Generate a route if no path segment was parsed
          current = current.add(new SegmentRoute(router, ""));
        }
      }

      public void query() {
        path = false;
      }
      public void queryParamLHS(CharSequence s, int from, int to) {
        String name = s.subSequence(from, to).toString();
        requestParam = RequestParam.builder().setName(name);
      }
      public void endQueryParam() {
        current.add(requestParam.build(router));
      }
      public void queryParamRHS() {
      }
      public void queryParamRHS(CharSequence s, int from, int to) {
        requestParam.matchByValue(s.subSequence(from, to).toString());
      }
      public void openExpr() {
        if (path) {
          paramDesc = PathParam.builder();
          if (!lastSegment) {
            chunks.add("");
          }
        }
      }
      public void pattern(CharSequence s, int from, int to) {
        if (path) {
          paramDesc.setPattern(s.subSequence(from, to).toString());
        } else {
          requestParam.matchByPattern(s.subSequence(from, to).toString());
        }
      }
      public void modifiers(CharSequence s, int from, int to) {
        while (from < to) {
          char modifier = s.charAt(from++);
          if (path) {
            switch (modifier) {
              // Capture group
              case 'c':
                paramDesc.setCaptureGroup(true);
                break;
              // Preserve path
              case 'p':
                paramDesc.preservePath(true);
                break;
              default:
                throw new MalformedRouteException("Unrecognized modifier " + modifier);
            }
          } else {
            switch (modifier) {
              // Required
              case 'r':
                requestParam.required();
                break;
              // Optional
              case 'o':
                requestParam.optional();
                break;
              default:
                throw new MalformedRouteException("Unrecognized modifier " + modifier);
            }
          }
        }
      }
      public void ident(CharSequence s, int from, int to) {
        String parameterName = s.subSequence(from, to).toString();
        QualifiedName qn = QualifiedName.parse(parameterName);
        if (path) {
          paramDesc.setQualifiedName(qn);
        } else {
          requestParam.setQualifiedName(qn);
        }
      }
      public void closeExpr() {
        if (path) {
          lastSegment = false;

          //
          PathParam param = paramDesc.build(router);
          // Append routing regex to the route regex surrounded by a non capturing regex
          // to isolate routingRegex like a|b or a(.)b
          builder.expr("(?:").expr(param.routingRegex).expr(")");
          parameterPatterns.add(param);
        }
      }
    }

    //
    Assembler asm = new Assembler();
    try {
      RouteParser.parse(path, asm);
    }
    catch (SyntaxException e) {
      throw new MalformedRouteException(e);
    }

    // Add params
    for (Map.Entry<QualifiedName, String> entry : params.entrySet()) {
      asm.current.add(new RouteParam(entry.getKey(), entry.getValue()));
    }

    //
    return asm.current;
  }

  public Route addParam(String name, String value) {
    return addParam(QualifiedName.parse(name), value);
  }

  public Route addParam(QualifiedName name, String value) {
    add(new RouteParam(name, value));
    return this;
  }

  private Param getParam(QualifiedName name) {
    Param param = routeParamMap.get(name);
    if (param == null) {
      for (RequestParam requestParam : requestParamArray) {
        if (requestParam.name.equals(name)) {
          param = requestParam;
          break;
        }
      }
      if (param == null && this instanceof PatternRoute) {
        for (PathParam pathParam : ((PatternRoute)this).params) {
          if (pathParam.name.equals(name)) {
            param = pathParam;
            break;
          }
        }
      }
    }
    return param;
  }

  private Param findParam(QualifiedName name) {
    Param param = getParam(name);
    if (param == null && parent != null) {
      param = parent.findParam(name);
    }
    return param;
  }

  private void findParams(List<Param> params) {
    Collections.addAll(params, routeParamArray);
    Collections.addAll(params, requestParamArray);
    if (this instanceof PatternRoute) {
      Collections.addAll(params, ((PatternRoute)this).params);
    }
  }

  private void findAncestorOrSelfParams(List<Param> params) {
    findParams(params);
    if (parent != null) {
      parent.findAncestorOrSelfParams(params);
    }
  }

  /**
   * Find the params having the specified <code>name</code> among this route or its descendants.
   *
   * @param name   the name
   * @param params the list collecting the found params
   */
  private void findDescendantOrSelfParams(QualifiedName name, List<Param> params) {
    Param param = getParam(name);
    if (param != null) {
      params.add(param);
    }
  }
}
