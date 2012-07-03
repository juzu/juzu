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

package juzu.impl.router.metadata;

import juzu.impl.router.QualifiedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes a route.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RouteDescriptor {

  /** . */
  private final String path;

  /** . */
  private final Map<QualifiedName, RouteParamDescriptor> routeParams;

  /** . */
  private final Map<QualifiedName, PathParamDescriptor> pathParams;

  /** . */
  private final Map<String, RequestParamDescriptor> requestParams;

  /** . */
  private final List<RouteDescriptor> children;

  public RouteDescriptor(String path) {
    if (path == null) {
      throw new NullPointerException("Was not expecting a null path");
    }

    //
    this.path = path;
    this.routeParams = new HashMap<QualifiedName, RouteParamDescriptor>();
    this.pathParams = new HashMap<QualifiedName, PathParamDescriptor>();
    this.requestParams = new HashMap<String, RequestParamDescriptor>();
    this.children = new ArrayList<RouteDescriptor>();
  }

  public String getPath() {
    return path;
  }

  public Set<QualifiedName> getRouteParamNames() {
    return routeParams.keySet();
  }

  public Collection<RouteParamDescriptor> getRouteParams() {
    return routeParams.values();
  }

  public RouteParamDescriptor getRouteParam(QualifiedName name) {
    return routeParams.get(name);
  }

  public RouteDescriptor with(ParamDescriptor... params) {
    if (params == null) {
      throw new NullPointerException();
    }
    for (ParamDescriptor param : params) {
      if (param == null) {
        throw new IllegalArgumentException();
      }
      if (param instanceof RouteParamDescriptor) {
        RouteParamDescriptor routeParam = (RouteParamDescriptor)param;
        routeParams.put(routeParam.getQualifiedName(), routeParam);
      }
      else if (param instanceof RequestParamDescriptor) {
        RequestParamDescriptor requestParam = (RequestParamDescriptor)param;
        requestParams.put(requestParam.getName(), requestParam);
      }
      if (param instanceof PathParamDescriptor) {
        PathParamDescriptor pathParam = (PathParamDescriptor)param;
        pathParams.put(pathParam.getQualifiedName(), pathParam);
      }
    }
    return this;
  }

  public Collection<RequestParamDescriptor> getRequestParams() {
    return requestParams.values();
  }

  public Set<String> getRequestParamMatchNames() {
    return requestParams.keySet();
  }

  public RequestParamDescriptor getRequestParam(String matchName) {
    return requestParams.get(matchName);
  }

  public Map<QualifiedName, PathParamDescriptor> getPathParams() {
    return pathParams;
  }

  public RouteDescriptor sub(RouteDescriptor child) {
    children.add(child);
    return this;
  }

  public List<RouteDescriptor> getChildren() {
    return children;
  }
}
