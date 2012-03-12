package org.juzu.impl.plugin.binding;

import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;

import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingDescriptor extends Descriptor
{

   /** . */
   private final ArrayList<BeanDescriptor> beans;

   public BindingDescriptor(ArrayList<BeanDescriptor> beans)
   {
      this.beans = beans;
   }

   @Override
   public Iterable<BeanDescriptor> getBeans()
   {
      return beans;
   }
}
