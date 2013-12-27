@Modules(
  value = {
    @Module(@Asset(id = "Foo", value = "foo.js")),
    @Module(@Asset(id = "Bar", value = "js/bar.js", location = AssetLocation.SERVER))
  })
@juzu.Application
package plugin.amd.location;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Asset;