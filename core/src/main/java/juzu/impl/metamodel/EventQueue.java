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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EventQueue implements Serializable {

  /** . */
  private final LinkedList<MetaModelEvent> events;

  public EventQueue(EventQueue that) {
    this.events = new LinkedList<MetaModelEvent>(that.events);
  }

  public EventQueue() {
    this.events = new LinkedList<MetaModelEvent>();
  }

  public List<MetaModelEvent> clear() {
    ArrayList<MetaModelEvent> copy = new ArrayList<MetaModelEvent>(events);
    events.clear();
    return copy;
  }

  public MetaModelEvent popEvent() {
    return events.isEmpty() ? null : events.removeFirst();
  }

  public boolean hasEvents() {
    return !events.isEmpty();
  }

  public void queue(MetaModelEvent event) {
    events.add(event);
  }

}
