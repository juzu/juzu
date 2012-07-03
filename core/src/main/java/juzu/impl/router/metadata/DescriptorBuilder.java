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

import juzu.impl.router.ControlMode;
import juzu.impl.router.ValueMapping;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptorBuilder {

  // http://www.gatein.org/xml/ns/gatein_controller_1_0

  public static PathParamDescriptor pathParam(String qualifiedName) {
    return new PathParamDescriptor(qualifiedName);
  }

  public static RequestParamDescriptor requestParam(String qualifiedName) {
    return new RequestParamDescriptor(qualifiedName);
  }

  public static RouteParamDescriptor routeParam(String qualifiedName) {
    return new RouteParamDescriptor(qualifiedName);
  }

  public static RouteDescriptor route(String path) {
    return new RouteDescriptor(path);
  }

  public static ControllerDescriptor router() {
    return new ControllerDescriptor();
  }

/*
   public ControllerDescriptor build(InputStream in) throws StaxNavException
   {
      return build(StaxNavigatorFactory.create(new Naming.Enumerated.Simple<Element>(Element.class, Element.UNKNOWN), in));
   }

   public ControllerDescriptor build(Reader reader) throws StaxNavException
   {
      return build(StaxNavigatorFactory.create(new Naming.Enumerated.Simple<Element>(Element.class, Element.UNKNOWN), reader));
   }

   public ControllerDescriptor build(XMLStreamReader reader) throws StaxNavException
   {
      return build(StaxNavigatorFactory.create(new Naming.Enumerated.Simple<Element>(Element.class, Element.UNKNOWN), reader));
   }

   public ControllerDescriptor build(StaxNavigator<Element> root) throws StaxNavException
   {
      ControllerDescriptor router = router();

      //
      String s = root.getAttribute("separator-escape");
      if (s != null)
      {
         char c = s.charAt(0);
         router.setSeparatorEscape(c);
      }

      //
      if (root.child() != null)
      {
         for (StaxNavigator<Element> routeNav : root.fork(Element.ROUTE))
         {
            RouteDescriptor route = buildRoute(routeNav);
            router.add(route);
         }
      }
      return router;
   }

   private RouteDescriptor buildRoute(StaxNavigator<Element> root) throws StaxNavException
   {
      String path = root.getAttribute("path");

      //
      RouteDescriptor route = new RouteDescriptor(path);

      //
      for (Element elt = root.child();elt != null;elt = root.sibling())
      {
         StaxNavigator<Element> fork = root.fork();
         switch (elt)
         {
            case PATH_PARAM:
            {
               String qualifiedName = fork.getAttribute("qname");
               String encoded = fork.getAttribute("encoding");
               boolean captureGroup = "true".equals(fork.getAttribute("capture-group"));
               String pattern = null;
               if (fork.child(Element.PATTERN))
               {
                  pattern = fork.getContent();
               }
               EncodingMode encodingMode = "preserve-path".equals(encoded) ? EncodingMode.PRESERVE_PATH : EncodingMode.FORM;
               route.with(new PathParamDescriptor(qualifiedName).captureGroup(captureGroup).encodedBy(encodingMode).matchedBy(pattern));
               break;
            }
            case ROUTE_PARAM:
            {
               String qualifiedName = fork.getAttribute("qname");
               String value = null;
               if (fork.child(Element.VALUE))
               {
                  value = fork.getContent();
               }
               route.with(new RouteParamDescriptor(qualifiedName).withValue(value));
               break;
            }
            case REQUEST_PARAM:
            {
               String qualifiedName = fork.getAttribute("qname");
               String name = fork.getAttribute("name");
               String controlModeAtt = fork.getAttribute("control-mode");
               String valueMappingAtt = fork.getAttribute("value-mapping");
               RequestParamDescriptor param = new RequestParamDescriptor(qualifiedName);
               param.setName(name);
               param.setControlMode(parseControlMode(controlModeAtt));
               param.setValueMapping(parseValueMapping(valueMappingAtt));
               if (fork.child(Element.VALUE))
               {
                  param.setValue(fork.getContent());
                  param.setValueType(ValueType.LITERAL);
               }
               if (fork.child(Element.PATTERN))
               {
                  param.setValue(fork.getContent());
                  param.setValueType(ValueType.PATTERN);
               }
               route.with(param);
               break;
            }
            case ROUTE:
               RouteDescriptor sub = buildRoute(fork);
               route.sub(sub);
               break;
            default:
               throw new AssertionError();
         }
      }

      //
      return route;
   }
*/

  static ControlMode parseControlMode(String s) {
    if (s == null || "optional".equals(s)) {
      return ControlMode.OPTIONAL;
    }
    else if ("required".equals(s)) {
      return ControlMode.REQUIRED;
    }
    else {
      throw new UnsupportedOperationException("Handle me gracefully");
    }
  }

  static ValueMapping parseValueMapping(String s) {
    if (s == null || "canonical".equals(s)) {
      return ValueMapping.CANONICAL;
    }
    else if ("never-empty".equals(s)) {
      return ValueMapping.NEVER_EMPTY;
    }
    else if ("never-null".equals(s)) {
      return ValueMapping.NEVER_NULL;
    }
    else {
      throw new UnsupportedOperationException("Handle me gracefully");
    }
  }
}
