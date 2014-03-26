@juzu.Application
@Bindings({@Binding(plugin.shiro.SimpleRealm.class)})
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}, rememberMe = true) 
package plugin.shiro.authc.remember;
import juzu.plugin.binding.*;
import juzu.plugin.shiro.*;
