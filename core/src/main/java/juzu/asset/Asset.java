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
    return uri(AssetLocation.EXTERNAL, uri);
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
