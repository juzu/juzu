@Defines(value = {
    @Define(@Asset(id = "Foo", value = "foo.js"))
})
@Modules(value = {
    @Module(
        value = @Asset(id = "Bar", value = "bar.js", depends = {"Foo"}),
        dependencies = {@Dependency(id = "Foo", alias = "foo")}
    )
})
@juzu.Application
package plugin.amd.mix;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Asset;