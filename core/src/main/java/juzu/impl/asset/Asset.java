/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
   *
   * @param uri the asset uri
   * @return the asset
   */
  public static Asset url(String uri) {
    return of(AssetLocation.URL, uri);
  }

  /**
   * Returns an asset.
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
