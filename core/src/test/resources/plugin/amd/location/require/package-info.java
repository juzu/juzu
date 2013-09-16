@Requires (
  value = {
    @Require(id = "Foo", path = "foo.js"), 
    @Require(id = "Bar", path = "js/bar.js", location = AssetLocation.SERVER)})
@juzu.Application
package plugin.amd.location.require;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;