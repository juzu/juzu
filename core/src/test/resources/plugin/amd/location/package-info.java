@Modules({
    @Module(id = "Foo", value = "foo.js"),
    @Module(id = "Bar", value = "js/bar.js", location = AssetLocation.SERVER, depends = {"Foo"})
  })
@juzu.Application
@Assets("Bar")
package plugin.amd.location;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;