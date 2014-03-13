@Modules({
    @Module(id = "Foo", value = "foo.js"),
    @Module(id = "Bar", value = "bar.js", depends = {"Foo"})
})
@juzu.Application
@WithAssets("Bar")
package plugin.amd.dependencies;
import juzu.plugin.amd.*;
import juzu.plugin.asset.WithAssets;