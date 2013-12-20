@Defines(
  value = {
    @Define(@Asset(id = "Foo", value = "foo.js")),
    @Define(@Asset(id = "Bar", value = "js/bar.js", location = AssetLocation.SERVER))
  })
@juzu.Application
package plugin.amd.location.define;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Asset;