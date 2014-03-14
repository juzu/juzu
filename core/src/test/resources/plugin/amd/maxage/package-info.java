@Modules(value = @Module(id = "Foo", value = "foo.js"), maxAge = 1000)
@juzu.Application
package plugin.amd.maxage;
import juzu.plugin.amd.*;
import juzu.plugin.asset.WithAssets;