package org.sample.controllers;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SessionScoped
public class Counter implements Serializable
{

   /** . */
   private int value;

   public void increment()
   {
      value++;
   }

   public int getValue()
   {
      return value;
   }
}
