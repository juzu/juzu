@juzu.Application
@Bindings({@Binding(plugin.shiro.SimpleRealm.class), @Binding(plugin.shiro.OtherRealm.class)})
@Shiro(realms = {
   @Realm(value = plugin.shiro.SimpleRealm.class, name = "simple"),
   @Realm(value = plugin.shiro.OtherRealm.class, name = "other")
}) 
package plugin.shiro.realms;
import juzu.plugin.shiro.*;
import juzu.plugin.binding.*;