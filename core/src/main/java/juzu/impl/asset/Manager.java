package juzu.impl.asset;

import juzu.asset.AssetType;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Qualifier
@Retention(RUNTIME)
public @interface Manager
{

   AssetType value();

}
