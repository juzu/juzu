@Modules(@Module(id = "Foo", value = "foo.js"))
@juzu.Application
@WithAssets("Foo")
package plugin.amd.module;
import juzu.plugin.amd.*;
import juzu.plugin.asset.WithAssets;