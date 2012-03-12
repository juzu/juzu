package org.juzu.impl.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EventQueue implements Serializable
{

   /** . */
   private final LinkedList<MetaModelEvent> events = new LinkedList<MetaModelEvent>();

   public List<MetaModelEvent> clear()
   {
      ArrayList<MetaModelEvent> copy = new ArrayList<MetaModelEvent>(events);
      events.clear();
      return copy;
   }

   public MetaModelEvent popEvent()
   {
      return events.isEmpty() ? null : events.removeFirst();
   }

   public boolean hasEvents()
   {
      return !events.isEmpty();
   }

   public void queue(MetaModelEvent event)
   {
      events.add(event);
   }

}
