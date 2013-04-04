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

package juzu.impl.metamodel;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelEvent implements Serializable {

  public static MetaModelEvent createAdded(MetaModelObject object, Object payload) {
    return new MetaModelEvent(AFTER_ADD, object, payload);
  }

  public static MetaModelEvent createAdded(MetaModelObject object) {
    return new MetaModelEvent(AFTER_ADD, object, null);
  }

  public static MetaModelEvent createUpdated(MetaModelObject object, Object payload) {
    return new MetaModelEvent(UPDATED, object, payload);
  }

  public static MetaModelEvent createUpdated(MetaModelObject object) {
    return new MetaModelEvent(UPDATED, object, null);
  }

  public static MetaModelEvent createRemoved(MetaModelObject object, Object payload) {
    return new MetaModelEvent(BEFORE_REMOVE, object, payload);
  }

  public static MetaModelEvent createRemoved(MetaModelObject object) {
    return new MetaModelEvent(BEFORE_REMOVE, object, null);
  }

  /** . */
  public static final int AFTER_ADD = 0;

  /** . */
  public static final int BEFORE_REMOVE = 1;

  /** . */
  public static final int UPDATED = 2;

  /** . */
  private final int type;

  /** . */
  private final MetaModelObject object;

  /** . */
  private final Object payload;

  private MetaModelEvent(int type, MetaModelObject object, Object payload) {
    if (type < 0 || type > 3) {
      throw new IllegalArgumentException();
    }
    if (object == null) {
      throw new NullPointerException();
    }

    //
    this.type = type;
    this.object = object;
    this.payload = payload;
  }

  public int getType() {
    return type;
  }

  public MetaModelObject getObject() {
    return object;
  }

  public Object getPayload() {
    return payload;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof MetaModelEvent) {
      MetaModelEvent that = (MetaModelEvent)obj;
      return type == that.type && object.equals(that.object);
    }
    return false;
  }

  public String toString() {
    return getClass().getSimpleName() + "[type=" + type + ",object=" + object.getClass().getSimpleName() + "]";
  }
}
