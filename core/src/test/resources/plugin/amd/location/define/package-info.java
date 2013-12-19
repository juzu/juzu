@Defines(
  value = {
    @Define(id = "Foo", path = "foo.js"),
    @Define(id = "Bar", path = "js/bar.js", location = AssetLocation.SERVER)})
@juzu.Application
package plugin.amd.location.define;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;