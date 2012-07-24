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
