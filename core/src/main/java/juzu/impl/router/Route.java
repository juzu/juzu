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

import juzu.impl.router.parser.RouteParser;
import juzu.impl.router.parser.RouteParserHandler;
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

  /** . */
  private static final int TERMINATION_NONE = 0;

  /** . */
  private static final int TERMINATION_SEGMENT = 1;

  /** . */
  private static final int TERMINATION_SEPARATOR = 2;

  /** . */
  private static final int TERMINATION_ANY = 3;

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
      writer.writeAttribute("terminal", Integer.toString(terminal));
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
  private final Router router;

  /** . */
  private Route parent;

  /** . */
  private List<Route> path;

  /** . */
  private int terminal;

  /** . */
  private Route[] children;

  Route(Router router) {

    // Invoked by Router subclass ... not pretty but simple and does the work
    if (router == null) {
      router = (Router)this;
    }

    //
    this.router = router;

    //
    this.path = Collections.singletonList(this);
    this.parent = null;
    this.terminal = TERMINATION_NONE;
    this.children = EMPTY_ROUTE_ARRAY;
  }

  /**
   * Clear this route of the children it may have, when cleared this route becomes a terminal route.
   */
  public final void clearChildren() {
    this.children = EMPTY_ROUTE_ARRAY;
  }

  /**
   * Returns the parent route or null when the route is the root route.
   *
   * @return the parent route
   */
  public final Route getParent() {
    return parent;
  }

  /**
   * Returns the path of routes from the root to this route.
   *
   * @return the path
   */
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

  public final RouteMatch matches(Map<QualifiedName, String> parameters) {

    //
    HashMap<QualifiedName, String> unmatched = new HashMap<QualifiedName, String>(parameters);
    HashMap<PathParam, String> matched = new HashMap<PathParam, String>();

    //
    if (_matches(unmatched, matched)) {
      return new RouteMatch(this, unmatched, matched);
    } else {
      return null;
    }
  }

  private boolean _matches(HashMap<QualifiedName, String> context, HashMap<PathParam, String> matched) {
    return (parent == null || parent._matches(context, matched)) && matches(context, matched);
  }

  private boolean matches(HashMap<QualifiedName, String> context, HashMap<PathParam, String> abc) {

    // Match any pattern parameter
    if (this instanceof PatternRoute) {
      PatternRoute prt = (PatternRoute)this;
      for (int i = 0;i < prt.params.length;i++) {
        PathParam param = prt.params[i];
        String s = context.get(param.name);
        String matched = null;
        if (s != null) {
          for (int j = 0;j < param.matchingRegex.length;j++) {
            RERef renderingRegex = param.matchingRegex[j];
            if (renderingRegex.re.matcher().matches(s)) {
              matched = param.templatePrefixes[j] + s + param.templateSuffixes[j];
              break;
            }
          }
        }
        if (matched != null) {
          context.remove(param.name);
          abc.put(param, matched);
        }
        else {
          return false;
        }
      }
    }

    //
    return true;
  }

  public final RouteMatch route(String path) {
    return route(path, Collections.<String, String[]>emptyMap());
  }

  public final RouteMatch route(String path, Map<String, String[]> queryParams) {
    Iterator<RouteMatch> matcher = matcher(path, queryParams);
    RouteMatch found = null;
    if (matcher.hasNext()) {
      found = matcher.next();

      //Select a route if more than one
      while (matcher.hasNext()) {
        RouteMatch match = matcher.next();
        Iterator<Route> foundI = found.getRoute().getPath().iterator();
        Iterator<Route> matchI = match.getRoute().getPath().iterator();

        // Drop the root route
        foundI.next();
        matchI.next();

        //
        while (foundI.hasNext()) {
          Route foundR = foundI.next();
          if (matchI.hasNext()) {
            Route matchR = matchI.next();
            if (foundR instanceof SegmentRoute) {
              if (matchR instanceof SegmentRoute) {
                // Continue
              } else {
                break;
              }
            } else {
              if (matchR instanceof SegmentRoute) {
                found = match;
                break;
              } else {
                // Continue
              }
            }
          } else {
            break;
          }
        }
      }
    }
    return found;
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
    private Map<PathParam, String> matches;

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

    Map<PathParam, String> getParameters() {
      Map<PathParam, String> parameters = null;
      for (RouteFrame frame = this;frame != null;frame = frame.parent) {
        if (frame.matches != null) {
          if (parameters == null) {
            parameters = new HashMap<PathParam, String>();
          }
          parameters.putAll(frame.matches);
        }
      }
      return parameters != null ? parameters : Collections.<PathParam, String>emptyMap();
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
      Map<PathParam, String> parameters = next.getParameters();
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
        current.status = RouteFrame.Status.PROCESS_CHILDREN;
      }
      else if (current.status == RouteFrame.Status.PROCESS_CHILDREN) {
        if (current.childIndex < current.route.children.length) {
          Route child = current.route.children[current.childIndex++];

          // The next frame
          RouteFrame next;

          //
          if (child instanceof SegmentRoute) {
            SegmentRoute segmentRoute = (SegmentRoute)child;

            // Remove any leading slashes
            int POS = 0;
            while (POS < current.path.length() && current.path.charAt(POS) == '/') {
              POS++;
            }

            // Find the next '/' for determining the segment and next path
            // JULIEN : this can be computed multiple times
            int pos = current.path.indexOf('/', POS);
            if (pos == -1) {
              pos = current.path.length();
            }

            //
            String segment = current.path.getValue().substring(POS, pos);

            // Determine next path
            if (segmentRoute.name.equals(segment)) {
              // Lazy create next segment path
              // JULIEN : this can be computed multiple times
              Path nextSegmentPath = current.path.subPath(pos);

              // Delegate the process to the next route
              next = new RouteFrame(current, segmentRoute, nextSegmentPath);
            }
            else {
              next = null;
            }
          }
          else if (child instanceof PatternRoute) {
            PatternRoute patternRoute = (PatternRoute)child;

            // We skip one '/' , should we skip more ? this raise issues with path encoding that manages '/'
            // need to figure out later
            Path path = current.path;
            if (path.length() > 0 && path.charAt(0) == '/') {
              path = path.subPath(1);
            }

            //
            RE.Match[] matches = patternRoute.pattern.re.matcher().find(path.getValue());

            // We match
            if (matches.length > 0) {
              // Build next controller context
              int nextPos = matches[0].getEnd();
              Path nextPath = path.subPath(nextPos);

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
                        char c = path.charAt(from);
                        if (c == child.router.separatorEscape && path.getRawLength(from) == 1) {
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
                      next.matches = new HashMap<PathParam, String>();
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

          //
          RouteFrame.Status next;

          // Anything that does not begin with '/' returns null


          int pos = 0;
          while (pos < current.path.length() && current.path.charAt(pos) == '/') {
            pos++;
          }

          // Are we done ?
          if (pos == current.path.length()) {
            switch (current.route.terminal) {
              case TERMINATION_NONE:
                next = RouteFrame.Status.END;
                break;
              case TERMINATION_SEGMENT:
                next = pos == 0 ? RouteFrame.Status.MATCHED : RouteFrame.Status.END;
                break;
              case TERMINATION_SEPARATOR:
                next = pos == 0 ? RouteFrame.Status.END : RouteFrame.Status.MATCHED;
                break;
              case TERMINATION_ANY:
                next = RouteFrame.Status.MATCHED;
                break;
              default:
                throw new AssertionError();
            }
          } else {
            next = RouteFrame.Status.END;
          }

          //
          current.status = next;
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
    LinkedList<PathParam> ancestorParams = new LinkedList<PathParam>();
    findAncestorOrSelfParams(ancestorParams);
    LinkedList<PathParam> descendantParams = new LinkedList<PathParam>();
    for (PathParam param : ancestorParams) {
      route.findDescendantOrSelfParams(param.name, descendantParams);
      if (descendantParams.size() > 0) {
        throw new MalformedRouteException("Duplicate parameter " + param.name);
      }
    }

    //
    if (route instanceof PatternRoute || route instanceof SegmentRoute) {
      children = Tools.appendTo(children, route);

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

  public Route append(String path) {
    return append(path, RouteKind.MATCH);
  }

  public Route append(String path, final RouteKind kind) {

    //
    class Assembler implements RouteParserHandler {
      Route current = Route.this;
      PatternBuilder builder = new PatternBuilder();
      List<String> chunks = new ArrayList<String>();
      List<PathParam> parameterPatterns = new ArrayList<PathParam>();
      PathParam.Builder paramDesc;
      boolean lastSegment;
      public void segmentOpen() {
        builder.clear().expr("");
        chunks.clear();
        parameterPatterns.clear();
        lastSegment = false;
      }
      public void segmentChunk(CharSequence s, int from, int to) {
        builder.litteral(s, from, to);
        chunks.add(s.subSequence(from, to).toString());
        lastSegment = true;
      }
      public void segmentClose() {
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
          // - the match expression is the empty expression
          builder.expr("(?:(?<=^)|(?=/)|$)");
          next = new PatternRoute(router, router.compile(builder.build()), parameterPatterns, chunks);
        }

        //
        current = current.add(next);
      }

      public void pathClose(boolean slash) {
        // Set terminal status
        switch (kind) {
          case CONNECT:
            current.terminal = TERMINATION_NONE;
            break;
          case MATCH:
            current.terminal = slash ? TERMINATION_SEPARATOR : TERMINATION_SEGMENT;
            break;
          case MATCH_ANY:
            current.terminal = TERMINATION_ANY;
            break;
        }
      }

      public void query() {
      }
      public void queryParamLHS(CharSequence s, int from, int to) {
      }
      public void queryParamClose() {
      }
      public void queryParamRHS() {
      }
      public void queryParamRHS(CharSequence s, int from, int to) {
      }
      public void exprOpen() {
        paramDesc = PathParam.builder();
        if (!lastSegment) {
          chunks.add("");
        }
      }
      public void exprPattern(CharSequence s, int from, int to) {
        paramDesc.setPattern(s.subSequence(from, to).toString());
      }
      public void exprModifiers(CharSequence s, int from, int to) {
        while (from < to) {
          char modifier = s.charAt(from++);
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
        }
      }
      public void exprIdent(CharSequence s, int from, int to) {
        String parameterName = s.subSequence(from, to).toString();
        QualifiedName qn = QualifiedName.parse(parameterName);
        paramDesc.setQualifiedName(qn);
      }
      public void exprClose() {
        lastSegment = false;

        //
        PathParam param = paramDesc.build(router);
        // Append routing regex to the route regex surrounded by a non capturing regex
        // to isolate routingRegex like a|b or a(.)b
        builder.expr("(?:").expr(param.routingRegex).expr(")");
        parameterPatterns.add(param);
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

    //
    return asm.current;
  }

  private PathParam getParam(QualifiedName name) {
    PathParam param = null;
    if (param == null && this instanceof PatternRoute) {
      for (PathParam pathParam : ((PatternRoute)this).params) {
        if (pathParam.name.equals(name)) {
          param = pathParam;
          break;
        }
      }
    }
    return param;
  }

  private PathParam findParam(QualifiedName name) {
    PathParam param = getParam(name);
    if (param == null && parent != null) {
      param = parent.findParam(name);
    }
    return param;
  }

  private void findParams(List<PathParam> params) {
    if (this instanceof PatternRoute) {
      Collections.addAll(params, ((PatternRoute)this).params);
    }
  }

  private void findAncestorOrSelfParams(List<PathParam> params) {
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
  private void findDescendantOrSelfParams(QualifiedName name, List<PathParam> params) {
    PathParam param = getParam(name);
    if (param != null) {
      params.add(param);
    }
  }
}
