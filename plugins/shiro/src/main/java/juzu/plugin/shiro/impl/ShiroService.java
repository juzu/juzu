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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;

import juzu.Response;
import juzu.Scope;
import juzu.asset.AssetLocation;
import juzu.impl.common.JSON;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;

import juzu.impl.request.Stage;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroService extends ApplicationService implements RequestFilter<Stage.Handler> {

  /** . */
  SecurityManager manager;

  /** . */
  private ShiroDescriptor descriptor;

  public ShiroService() {
    super("shiro");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    return context.getConfig() != null ? descriptor = new ShiroDescriptor(context) : null;
  }

  @PostConstruct
  public void postConstruct() throws ConfigurationException, IOException {
    URL iniURL = getShiroIniURL();
    if (iniURL != null) {
      Ini ini = new Ini();
      ini.load(iniURL.openStream());
      IniSecurityManagerFactory factory = new IniSecurityManagerFactory(ini);
      manager = factory.getInstance();
    }
  }

  @Override
  public Class<Stage.Handler> getStageType() {
    return Stage.Handler.class;
  }

  @Override
  public Response handle(Stage.Handler stage) {
    if (descriptor != null) {
      try {
        if (manager == null) {
          SecurityManagerProvider provider = new SecurityManagerProvider(descriptor.getConfig());
          manager = provider.get();
        }
        start();
        return descriptor.invoke(stage);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } finally {
        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
      }
    } else {
      return stage.invoke();
    }
  }

  private void start() throws InvocationTargetException {
    //
    Request request = Request.getCurrent();
    Subject currentUser = null;

    if (request.getScopeController().get(Scope.SESSION, "currentUser") != null) {
      currentUser = (Subject)request.getScopeController().get(Scope.SESSION, "currentUser").get();
    } else {
      Subject.Builder builder = new Subject.Builder(manager);
      currentUser = builder.buildSubject();
      SubjectScoped subjectValue = new SubjectScoped(currentUser);
      request.getScopeController().put(Scope.SESSION, "currentUser", subjectValue);
    }

    //
    ThreadContext.bind(manager);
    ThreadContext.bind(currentUser);
  }

  private URL getShiroIniURL() throws MalformedURLException {
    JSON json = descriptor.getConfig().getJSON("config");

    if (json == null)
      return null;

    AssetLocation location = AssetLocation.APPLICATION;
    if (json.get("location") != null) {
      location = AssetLocation.valueOf(json.getString("location"));
    }

    switch (location) {
      case APPLICATION :
        return descriptor.getContext().getApplicationResolver().resolve(json.getString("value"));
      case SERVER :
        return descriptor.getContext().getServerResolver().resolve(json.getString("value"));
      case URL :
        return new URL(json.getString("value"));
      default :
        return null;
    }
  }
}
