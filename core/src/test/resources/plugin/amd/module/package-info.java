@Modules(value = {
    @Module(@Asset(id = "Foo", value = "foo.js"))
})
@juzu.Application
package plugin.amd.module;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Asset;