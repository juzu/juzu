/*
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
package juzu.plugin.shiro.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import juzu.impl.common.JSON;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.HandlerMetaModel;
import juzu.plugin.shiro.Login;
import juzu.plugin.shiro.Logout;
import juzu.plugin.shiro.Shiro;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroMetaModelPlugin extends ApplicationMetaModelPlugin {
  /** . */
  private final Map<ElementHandle.Package, JSON> enableMap = new HashMap<ElementHandle.Package, JSON>();

  /** . */
  private final Map<ElementHandle<?>, Map<AnnotationKey, AnnotationState>> methods =
    new HashMap<ElementHandle<?>, Map<AnnotationKey, AnnotationState>>();

  /** . */
  private final Map<ElementHandle<?>, Map<AnnotationKey, AnnotationState>> controllers =
    new HashMap<ElementHandle<?>, Map<AnnotationKey, AnnotationState>>();

  public ShiroMetaModelPlugin() {
    super("shiro");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.<Class<? extends java.lang.annotation.Annotation>> set(Shiro.class, RequiresGuest.class,
      RequiresUser.class, RequiresAuthentication.class, RequiresPermissions.class, RequiresRoles.class, Login.class,
      Logout.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    ElementHandle.Package handle = metaModel.getHandle();
    if (key.getType().equals(Name.create(Shiro.class))) {
      JSON json = new JSON();
      json.set("rememberMe", added.get("rememberMe"));
      json.set("config", added.get("config"));
      List<AnnotationState> realms = (List<AnnotationState>)added.get("realms");
      JSON realmsJSON = new JSON();
      if (realms != null) {
        for (AnnotationState sel : realms) {
          ElementHandle.Type clazz = (juzu.impl.compiler.ElementHandle.Type)sel.get("value");
          String name = (String)sel.get("name");
          realmsJSON.set(clazz.getName().toString(), new JSON().set("name", name));
        }
      }
      json.set("realms", realmsJSON);
      enableMap.put(handle, json);
    } else {
      emitConfig(key, added);
    }
  }

  private void emitConfig(AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(Name.create(RequiresGuest.class)) || key.getType().equals(Name.create(RequiresUser.class))
      || key.getType().equals(Name.create(RequiresAuthentication.class))
      || key.getType().equals(Name.create(RequiresRoles.class))
      || key.getType().equals(Name.create(RequiresPermissions.class)) || key.getType().equals(Name.create(Login.class))
      || key.getType().equals(Name.create(Logout.class))) {
      if (key.getElement() instanceof ElementHandle.Method) {
        Map<AnnotationKey, AnnotationState> annotations = methods.get(key.getElement());
        if (annotations == null) {
          annotations = new HashMap<AnnotationKey, AnnotationState>();
          methods.put(key.getElement(), annotations);
        }
        annotations.put(key, added);
      } else if (key.getElement() instanceof ElementHandle.Type) {
        if (key.getType().equals(Name.create(RequiresGuest.class))
          || key.getType().equals(Name.create(RequiresAuthentication.class))
          || key.getType().equals(Name.create(RequiresUser.class))) {
          Map<AnnotationKey, AnnotationState> annotations = controllers.get(key.getElement());
          if (annotations == null) {
            annotations = new HashMap<AnnotationKey, AnnotationState>();
            controllers.put(key.getElement(), annotations);
          }
          annotations.put(key, added);
        } else
          throw new UnsupportedOperationException("Unsupported " + key.getType() + " at " + key.getElement());
      }
    }
  }

  private void emitConfig(JSON json, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(Name.create(Login.class))) {
      if (json.get("operator") != null) {
        throw new UnsupportedOperationException("Unsupported multiple operators at " + key.getElement());
      }
      json.set("operator", "login");
    } else if (key.getType().equals(Name.create(Logout.class))) {
      if (json.get("operator") != null) {
        throw new UnsupportedOperationException("Unsupported multiple operators at " + key.getElement());
      }
      json.set("operator", "logout");
    } else if (key.getType().equals(Name.create(RequiresGuest.class))) {
      if (json.get("require") != null || json.get("permissions") != null || json.get("roles") != null) {
        throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
      }
      json.set("require", "guest");
    } else if (key.getType().equals(Name.create(RequiresUser.class))) {
      if (json.get("require") != null || json.get("permissions") != null || json.get("roles") != null) {
        throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
      }
      json.set("require", "user");
    } else if (key.getType().equals(Name.create(RequiresAuthentication.class))) {
      if (json.get("require") != null || json.get("permissions") != null || json.get("roles") != null) {
        throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
      }
      json.set("require", "authenticate");
    } else if (key.getType().equals(Name.create(RequiresPermissions.class))) {
      if (json.get("require") != null) {
        throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
      }
      ArrayList<String> values = (ArrayList<String>)added.get("value");
      String logical = (String)added.get("logical");
      JSON foo = new JSON();
      foo.set("value", values);
      if (logical != null) {
        foo.set("logical", logical);
      } else {
        foo.set("logical", Logical.AND);
      }
      json.set("permissions", foo);
    } else if (key.getType().equals(Name.create(RequiresRoles.class))) {
      if (json.get("require") != null) {
        throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
      }
      ArrayList<String> values = (ArrayList<String>)added.get("value");
      String logical = (String)added.get("logical");
      JSON foo = new JSON();
      foo.set("value", values);
      if (logical != null) {
        foo.set("logical", logical);
      } else {
        foo.set("logical", Logical.AND);
      }
      json.set("roles", foo);
    }
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel metaModel) {
    ElementHandle.Package packageHandle = metaModel.getHandle();
    JSON config = enableMap.get(packageHandle);
    if (config != null) {
      ControllersMetaModel controllersModel = metaModel.getChild(ControllersMetaModel.KEY);

      for (ControllerMetaModel controller : controllersModel) {
        Map<AnnotationKey, AnnotationState> annotations = controllers.get(controller.getHandle());
        if (annotations != null) {
          JSON controllerJSON = new JSON();
          config.set(controller.getHandle().getName().toString(), controllerJSON);

          for (Map.Entry<AnnotationKey, AnnotationState> entry : annotations.entrySet()) {
            AnnotationKey key = entry.getKey();

            if (controllerJSON.get("require") != null) {
              throw new UnsupportedOperationException("Unsupported multiple requirements at " + key.getElement());
            }

            if (key.getType().equals(Name.create(RequiresGuest.class))) {
              controllerJSON.set("require", "guest");
            } else if (key.getType().equals(Name.create(RequiresAuthentication.class))) {
              controllerJSON.set("require", "authenticate");
            } else if (key.getType().equals(Name.create(RequiresUser.class))) {
              controllerJSON.set("require", "user");
            }
          }
        }

        for (HandlerMetaModel handler : controller) {
          annotations = methods.get(handler.getMethod());
          String methodId = handler.getMethod().getMethodHandle().toString();

          if (annotations != null) {
            JSON controllerJSON = config.getJSON(controller.getHandle().getName().toString());
            if (controllerJSON == null) {
              controllerJSON = new JSON();
              config.set(controller.getHandle().getName().toString(), controllerJSON);
            }

            JSON methodJSON = new JSON();;
            for (Map.Entry<AnnotationKey, AnnotationState> entry : annotations.entrySet()) {
              emitConfig(methodJSON, entry.getKey(), entry.getValue());
            }

            JSON methodsJSON = controllerJSON.getJSON("methods");
            if (methodsJSON == null) {
              methodsJSON = new JSON();
              controllerJSON.set("methods", methodsJSON);
            }
            methodsJSON.set(methodId.substring(methodId.lastIndexOf('#') + 1), methodJSON);
          }
        }
      }
    }
  }

  @Override
  public void destroy(ApplicationMetaModel application) {
    enableMap.remove(application.getHandle());
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    return enableMap.get(application.getHandle());
  }
}
