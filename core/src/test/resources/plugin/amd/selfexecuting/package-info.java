@Modules({
    @Module(id = "Foo", value = "foo.js"),
    @Module(id = "Bar", value = "bar.js", depends = {"Foo"}, aliases = {"foo"})
})
@juzu.Application
@Assets("Bar")
package plugin.amd.selfexecuting;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Assets;