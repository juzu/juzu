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

//import javanet.staxutils.IndentingXMLStreamWriter;

import juzu.impl.router.metadata.PathParamDescriptor;
import juzu.impl.router.metadata.RequestParamDescriptor;
import juzu.impl.router.metadata.RouteDescriptor;
import juzu.impl.router.metadata.RouteParamDescriptor;
import juzu.impl.utils.Tools;

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
class Route {

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
        writer.writeAttribute("encodingMode", param.encodingMode.toString());
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
      writer.writeAttribute("name", requestParam.matchName);
      if (requestParam.matchPattern != null) {
        writer.writeAttribute("value", requestParam.matchPattern.getPattern());
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
    this.router = router;
    this.parent = null;
    this.terminal = true;
    this.children = EMPTY_ROUTE_ARRAY;
    this.routeParamMap = Collections.emptyMap();
    this.routeParamArray = EMPTY_ROUTE_PARAM_ARRAY;
    this.requestParamMap = Collections.emptyMap();
    this.requestParamArray = EMPTY_REQUEST_PARAM_ARRAY;
  }

  final boolean isTerminal() {
    return terminal;
  }

  /*
  * Ok, so this is not the fastest way to do it, but for now it's OK, it's what is needed, we'll find
  * a way to optimize it later with some precompilation.
  */
  final void render(RenderContext context, URIWriter writer) throws IOException {
    RouteMatch r = find(context);

    // We found a route we need to render it now
    if (r != null) {
      r.render(writer);
    }
  }

  static class RouteMatch {

    /** The matched route. */
    final Route route;

    /** The matched parameters. */
    final Map<QualifiedName, String> matches;

    /** . */
    final RenderContext context;

    RouteMatch(RenderContext context, Route route, Map<QualifiedName, String> matches) {
      this.context = context;
      this.route = route;
      this.matches = matches;
    }

    private void render(URIWriter writer) throws IOException {
      // Append path first
      renderPath(route, writer, false);

      // Append query parameters after
      renderQueryString(route, writer);
    }

    private boolean renderPath(Route route, URIWriter writer, boolean hasChildren) throws IOException {
      boolean endWithSlash;
      if (route.parent != null) {
        endWithSlash = renderPath(route.parent, writer, true);
      }
      else {
        endWithSlash = false;
      }

      //
      if (route instanceof SegmentRoute) {
        SegmentRoute sr = (SegmentRoute)route;
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
      else if (route instanceof PatternRoute) {
        PatternRoute pr = (PatternRoute)route;
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
          String value = matches.get(def.name);
          count += value.length();

          // Write value
          for (int len = value.length(), j = 0;j < len;j++) {
            char c = value.charAt(j);
            if (c == route.router.separatorEscape) {
              if (def.encodingMode == EncodingMode.PRESERVE_PATH) {
                writer.append('_');
              }
              else {
                writer.append('%');
                writer.append(route.router.separatorEscapeNible1);
                writer.append(route.router.separatorEscapeNible2);
              }
            }
            else if (c == '/') {
              writer.append(def.encodingMode == EncodingMode.PRESERVE_PATH ? '/' : route.router.separatorEscape);
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

    private void renderQueryString(Route route, URIWriter writer) throws IOException {
      if (route.parent != null) {
        renderQueryString(route.parent, writer);
      }

      //
      for (RequestParam requestParamDef : route.requestParamArray) {
        String s = matches.get(requestParamDef.name);
        switch (requestParamDef.valueMapping) {
          case CANONICAL:
            break;
          case NEVER_EMPTY:
            if (s != null && s.length() == 0) {
              s = null;
            }
            break;
          case NEVER_NULL:
            if (s == null) {
              s = "";
            }
            break;
        }
        if (s != null) {
          writer.appendQueryParameter(requestParamDef.matchName, s);
        }
      }
    }
  }

  final RouteMatch find(RenderContext context) {
    context.enter();
    RouteMatch route = _find(context);
    context.leave();
    return route;
  }

  private RouteMatch _find(RenderContext context) {
    // Match first the static parameteters
    for (RouteParam param : routeParamArray) {
      RenderContext.Parameter entry = context.getParameter(param.name);
      if (entry != null && !entry.isMatched() && param.value.equals(entry.getValue())) {
        entry.remove(entry.getValue());
      }
      else {
        return null;
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
        entry.remove(entry.getValue());
      }
      else {
        switch (requestParamDef.controlMode) {
          case OPTIONAL:
            // Do nothing
            break;
          case REQUIRED:
            return null;
          default:
            throw new AssertionError();
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
          switch (param.encodingMode) {
            case FORM:
            case PRESERVE_PATH:
              for (int j = 0;j < param.matchingRegex.length;j++) {
                Regex renderingRegex = param.matchingRegex[j];
                if (context.matcher(renderingRegex).matches(s.getValue())) {
                  matched = param.templatePrefixes[j] + s.getValue() + param.templateSuffixes[j];
                  break;
                }
              }
              break;
            default:
              throw new AssertionError();
          }
        }
        if (matched != null) {
          s.remove(matched);
        }
        else {
          return null;
        }
      }
    }

    //
    if (context.isEmpty() && terminal) {
      Map<QualifiedName, String> matches = Collections.emptyMap();
      for (QualifiedName name : context.getNames()) {
        RenderContext.Parameter parameter = context.getParameter(name);
        if (matches.isEmpty()) {
          matches = new HashMap<QualifiedName, String>();
        }
        String match = parameter.getMatch();
        matches.put(name, match);
      }
      return new RouteMatch(context, this, matches);
    }

    //
    for (Route route : children) {
      RouteMatch a = route.find(context);
      if (a != null) {
        return a;
      }
    }

    //
    return null;
  }

  /**
   * Create a route matcher for the a request.
   *
   * @param path          the path
   * @param requestParams the query parameters
   * @return the route matcher
   */
  final RouteMatcher route(String path, Map<String, String[]> requestParams) {
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
    private Map<QualifiedName, String> matches;

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

    Map<QualifiedName, String> getParameters() {
      Map<QualifiedName, String> parameters = null;
      for (RouteFrame frame = this;frame != null;frame = frame.parent) {
        if (frame.matches != null) {
          if (parameters == null) {
            parameters = new HashMap<QualifiedName, String>();
          }
          parameters.putAll(frame.matches);
        }
        for (RouteParam param : frame.route.routeParamArray) {
          if (parameters == null) {
            parameters = new HashMap<QualifiedName, String>();
          }
          parameters.put(param.name, param.value);
        }
      }
      return parameters != null ? parameters : Collections.<QualifiedName, String>emptyMap();
    }
  }

  static class RouteMatcher implements Iterator<Map<QualifiedName, String>> {

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

    public Map<QualifiedName, String> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Map<QualifiedName, String> parameters = next.getParameters();
      next = null;
      return parameters;
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
          String[] values = requestParams.get(requestParamDef.matchName);
          if (values != null && values.length > 0 && values[0] != null) {
            value = values[0];
          }
          if (value == null) {
            switch (requestParamDef.controlMode) {
              case OPTIONAL:
                // Do nothing
                break;
              case REQUIRED:
                matched = false;
                break;
            }
          }
          else if (!requestParamDef.matchValue(value)) {
            matched = false;
            break;
          }
          switch (requestParamDef.valueMapping) {
            case CANONICAL:
              break;
            case NEVER_EMPTY:
              if (value != null && value.length() == 0) {
                value = null;
              }
              break;
            case NEVER_NULL:
              if (value == null) {
                value = "";
              }
              break;
          }
          if (value != null) {
            if (current.matches == null) {
              current.matches = new HashMap<QualifiedName, String>();
            }
            current.matches.put(requestParamDef.name, value);
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
            Regex.Match[] matches = patternRoute.pattern.matcher().find(current.path.getValue());

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
                  Regex.Match match = matches[index + j];
                  if (match.getEnd() != -1) {
                    String value;
                    if (param.encodingMode == EncodingMode.FORM) {
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
                      next.matches = new HashMap<QualifiedName, String>();
                    }
                    next.matches.put(param.name, value);
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
      route.parent = this;
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

  final Route append(RouteDescriptor descriptor) throws MalformedRouteException {
    Route route = append(descriptor.getPathParams(), descriptor.getPath());

    //
    for (RouteParamDescriptor routeParamDesc : descriptor.getRouteParams()) {
      route.add(RouteParam.create(routeParamDesc));
    }

    //
    for (RequestParamDescriptor requestParamDesc : descriptor.getRequestParams()) {
      route.add(RequestParam.create(requestParamDesc, router));
    }

    //
    for (RouteDescriptor childDescriptor : descriptor.getChildren()) {
      route.append(childDescriptor);
    }

    //
    return route;
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
    requestParamMap.put(param.matchName, param);
    requestParamArray = Tools.appendTo(requestParamArray, param);
    return this;
  }

  /**
   * Append a path, creates the necessary routes and returns the last route added.
   *
   * @param pathParamDescriptors the path param descriptors
   * @param path                 the path to append
   * @return the last route added
   */
  private Route append(Map<QualifiedName, PathParamDescriptor> pathParamDescriptors, String path) throws MalformedRouteException {
    if (path.length() == 0 || path.charAt(0) != '/') {
      throw new MalformedRouteException();
    }

    //
    int pos = path.length();
    int level = 0;
    List<Integer> start = new ArrayList<Integer>();
    List<Integer> end = new ArrayList<Integer>();
    for (int i = 1;i < path.length();i++) {
      char c = path.charAt(i);
      if (c == '{') {
        if (level++ == 0) {
          start.add(i);
        }
      }
      else if (c == '}') {
        if (--level == 0) {
          end.add(i);
        }
      }
      else if (c == '/') {
        if (level == 0) {
          pos = i;
          break;
        }
      }
    }

    //
    Route next;
    if (start.isEmpty()) {
      String segment = path.substring(1, pos);
      SegmentRoute route = new SegmentRoute(router, segment);
      add(route);
      next = route;
    }
    else {
      if (start.size() == end.size()) {
        PatternBuilder builder = new PatternBuilder();
        builder.expr("^").expr('/');
        List<String> chunks = new ArrayList<String>();
        List<PathParam> parameterPatterns = new ArrayList<PathParam>();

        //
        int previous = 1;
        for (int i = 0;i < start.size();i++) {
          builder.litteral(path, previous, start.get(i));
          chunks.add(path.substring(previous, start.get(i)));
          String parameterName = path.substring(start.get(i) + 1, end.get(i));

          //
          QualifiedName parameterQName = QualifiedName.parse(parameterName);

          // Now get path param metadata
          PathParamDescriptor parameterDescriptor = pathParamDescriptors.get(parameterQName);

          //
          PathParam param;
          if (parameterDescriptor != null) {
            param = PathParam.create(parameterDescriptor, router);
          }
          else {
            param = PathParam.create(parameterQName, router);
          }

          // Append routing regex to the route regex surrounded by a non capturing regex
          // to isolate routingRegex like a|b or a(.)b
          builder.expr("(?:").expr(param.routingRegex).expr(")");

          // Add the path param with the rendering regex
          parameterPatterns.add(param);
          previous = end.get(i) + 1;
        }

        //
        builder.litteral(path, previous, pos);

        // We want to satisfy one of the following conditions
        // - the next char after the matched expression is '/'
        // - the expression matched until the end
        // - the match expression is the '/' expression
        builder.expr("(?:(?<=^/)|(?=/)|$)");

        //
        chunks.add(path.substring(previous, pos));
        PatternRoute route = new PatternRoute(router, router.compile(builder.build()), parameterPatterns, chunks);

        // Wire
        add(route);

        //
        next = route;
      }
      else {
        throw new UnsupportedOperationException("Report error");
      }
    }

    //
    if (pos < path.length()) {
      return next.append(pathParamDescriptors, path.substring(pos));
    }
    else {
      return next;
    }
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
    for (RequestParam param : requestParamArray) {
      params.add(param);
    }
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
