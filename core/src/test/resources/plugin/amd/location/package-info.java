@Modules(
  value = {
    @Module(@Asset(id = "Foo", value = "foo.js")),
    @Module(@Asset(id = "Bar", value = "js/bar.js", location = AssetLocation.SERVER, depends = {"Foo"}))
  })
@juzu.Application
@WithAssets("Bar")
package plugin.amd.location;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.WithAssets;