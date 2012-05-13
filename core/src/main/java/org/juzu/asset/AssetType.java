package org.juzu.asset;

import org.juzu.impl.utils.Tools;

import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum AssetType
{

   STYLESHEET("css", "less"),

   SCRIPT("js");

   /** . */
   private final Set<String> extensions;

   private AssetType(String... extensions)
   {
      this.extensions = Collections.unmodifiableSet(Tools.set(extensions));
   }

   public Set<String> getExtensions()
   {
      return extensions;
   }
}
