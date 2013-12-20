@Defines(value = {
    @Define(@Asset(id = "Foo", value = "foo.js")),
    @Define(@Asset(id = "Bar", value = "bar.js"))
})
@juzu.Application
package plugin.amd.define;
import juzu.plugin.amd.*;
import juzu.plugin.asset.Asset;