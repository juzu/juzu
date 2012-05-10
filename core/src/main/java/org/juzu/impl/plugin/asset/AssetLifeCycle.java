package org.juzu.impl.plugin.asset;

import org.juzu.PropertyMap;
import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.request.Request;
import org.juzu.plugin.asset.Assets;
import org.juzu.plugin.asset.Script;
import org.juzu.plugin.asset.Stylesheet;
import org.juzu.request.Phase;

import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetLifeCycle extends RequestLifeCycle
{

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final String[] scripts;

   /** . */
   private final String[] stylesheets;

   @Inject
   public AssetLifeCycle(ApplicationDescriptor desc)
   {
      String[] scripts;
      String[] stylesheets;
      Class<?> packageClass = desc.getPackageClass();
      Assets assets = packageClass.getAnnotation(Assets.class);
      if (assets != null)
      {
         Script[] scriptDecls = assets.scripts();
         if (scriptDecls.length > 0)
         {
            scripts = new String[scriptDecls.length];
            for (int i = 0;i < scriptDecls.length;i++)
            {
               scripts[i] = scriptDecls[i].src();
            }
         }
         else
         {
            scripts = EMPTY_STRING_ARRAY;
         }
         Stylesheet[] stylesheetDecls = assets.stylesheets();
         if (stylesheetDecls.length > 0)
         {
            stylesheets = new String[stylesheetDecls.length];
            for (int i = 0;i < stylesheetDecls.length;i++)
            {
               stylesheets[i] = stylesheetDecls[i].src();
            }
         }
         else
         {
            stylesheets = EMPTY_STRING_ARRAY;
         }
      }
      else
      {
         scripts = EMPTY_STRING_ARRAY;
         stylesheets = EMPTY_STRING_ARRAY;
      }

      //
      this.stylesheets = stylesheets;
      this.scripts = scripts;
   }

   @Override
   public void invoke(Request request) throws ApplicationException
   {
      request.invoke();

      //
      if (request.getContext().getPhase() == Phase.RENDER)
      {
         Response response = request.getResponse();
         if (response instanceof Response.Render && (scripts.length > 0 || stylesheets.length > 0))
         {
            Response.Render render = (Response.Render)response;

            // Add assets
            PropertyMap properties = new PropertyMap(render.getProperties());
            properties.addValues(Response.Render.SCRIPT, scripts);
            properties.addValues(Response.Render.STYLESHEET, stylesheets);

            // Use a new response
            request.setResponse(new Response.Render(properties, render.getStreamable()));
         }
      }
   }
}
