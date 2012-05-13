package org.juzu.impl.plugin.asset;

import org.juzu.PropertyMap;
import org.juzu.Response;
import org.juzu.asset.AssetType;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.asset.AssetManager;
import org.juzu.impl.asset.AssetMetaData;
import org.juzu.impl.asset.Manager;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.request.Request;
import org.juzu.request.Phase;

import javax.inject.Inject;
import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetLifeCycle extends RequestLifeCycle
{

   /** . */
   private final String[] scripts;

   /** . */
   private final String[] stylesheets;

   @Inject
   public AssetLifeCycle(ApplicationDescriptor desc,
                         @Manager(AssetType.SCRIPT) AssetManager scriptManager,
                         @Manager(AssetType.STYLESHEET) AssetManager stylesheetManager)
   {
      AssetDescriptor descriptor = (AssetDescriptor)desc.getPlugin("asset");

      //
      ArrayList<String> scripts = new ArrayList<String>();
      for (AssetMetaData script : descriptor.getScripts())
      {
         String id = script.getId();
         if (id != null)
         {
            scripts.add(script.getId());
         }
         else
         {
            scripts.add(script.getValue());
         }
         scriptManager.addAsset(script);
      }

      //
      ArrayList<String> stylesheets = new ArrayList<String>();
      for (AssetMetaData stylesheet : descriptor.getStylesheets())
      {
         String id = stylesheet.getId();
         if (id != null)
         {
            stylesheets.add(stylesheet.getId());
         }
         else
         {
            stylesheets.add(stylesheet.getValue());
         }
         stylesheetManager.addAsset(stylesheet);
      }

      //
      this.scripts = scripts.toArray(new String[scripts.size()]);
      this.stylesheets = stylesheets.toArray(new String[stylesheets.size()]);
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
            properties.addValues(Response.Render.STYLESHEET, stylesheets);
            properties.addValues(Response.Render.SCRIPT, scripts);

            // Use a new response
            request.setResponse(new Response.Render(properties, render.getStreamable()));
         }
      }
   }
}
