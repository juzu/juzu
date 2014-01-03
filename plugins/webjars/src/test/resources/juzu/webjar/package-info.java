@Application
@WebJars(@WebJar("jquery"))
@Assets(@Asset("jquery/1.10.2/jquery.js"))
@WithAssets
package juzu.webjar;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.webjars.WebJar;