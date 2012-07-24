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

package juzu.asset;

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
public abstract class Asset {

  public static Ref ref(String id) {
    return new Ref(id);
  }

  public static Value uri(String uri) {
    return uri(AssetLocation.SERVER, uri);
  }

  public static Value uri(AssetLocation location, String uri) {
    return new Value(location, uri);
  }

  /**
   * A valued asset.
   */
  public static class Value extends Asset {

    /** . */
    private final AssetLocation location;

    /** . */
    private final String uri;

    private Value(AssetLocation location, String uri) {
      this.location = location;
      this.uri = uri;
    }

    public AssetLocation getLocation() {
      return location;
    }

    public String getURI() {
      return uri;
    }
  }

  /**
   * A referenced asset.
   */
  public static class Ref extends Asset {

    /** . */
    private final String id;

    private Ref(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }
}
