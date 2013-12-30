@Modules(value = {
    @Module(@Asset(id = "Foo", value = "foo.js")),
    @Module(
        value = @Asset(id = "Bar", value = "bar.js", depends = {"Foo"}),
        aliases = {"foo"}
    )
})
@juzu.Application
@WithAssets("Bar")
package plugin.amd.selfexecuting;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.WithAssets;