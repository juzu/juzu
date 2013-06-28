/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.asset;

import juzu.asset.AssetLocation;

/**
 * <p>Representation of an asset at runtime, an asset can be a reference or a value.</p>
 *
 * <p>Asset references are a mere reference to an asset that will be managed by the server, for instance the <code>jquery</code>
 * asset reference an asset for which the developer does not have to provide details.</p>
 *
 * <p>Asset values provide an explicit asset with a location and an URI that will be used to resolve fully the asset.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Asset {

  /** . */
  private final AssetLocation location;

  /** . */
  private final String uri;
  
  public Asset(AssetLocation location, String uri) {
    this.location = location;
    this.uri = uri;
  }

  /**
   * Wraps an URI as a server located asset.
   *
   * @param uri the asset uri
   * @return the asset
   */
  public static Asset server(String uri) {
    return of(AssetLocation.SERVER, uri);
  }

  /**
   * Wraps an URI as a application located asset.
   *
   * @param uri the asset uri
   * @return the asset
   */
  public static Asset application(String uri) {
    return of(AssetLocation.APPLICATION, uri);
  }

  /**
   * Wraps an URI as an absolute uri.
   * @param id the id of asset (only used by AMD for now)
   * @param uri the asset uri
   * @return the asset
   */
  public static Asset url(String id, String uri) {
    return of(AssetLocation.URL, uri);
  }

  /**
   * Returns an asset.
   *
   *
   * @param location the asset location
   * @param uri the asset uri
   * @return the asset
   */
  public static Asset of(AssetLocation location, String uri) {
    return new Asset(location, uri);
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getURI() {
    return uri;
  }
}
