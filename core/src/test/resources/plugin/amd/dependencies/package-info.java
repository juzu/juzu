@Modules(value = {
    @Module(@Asset(id = "Foo", value = "foo.js")),
    @Module(@Asset(id = "Bar", value = "bar.js", depends = {"Foo"}))
})
@juzu.Application
@WithAssets("Bar")
package plugin.amd.dependencies;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.WithAssets;