@Application
@WebJars(@WebJar("jquery"))
@Scripts(@Script("jquery/1.10.2/jquery.js"))
@Assets("*")
package juzu.webjar;

import juzu.Application;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Assets;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.webjars.WebJar;