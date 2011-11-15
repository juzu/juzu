/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package request.render.overridenindex;

import org.juzu.Controller;
import org.juzu.View;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller
{

   @View
   public void index() throws IOException
   {
      renderContext.getPrinter().write("0[" + A_.indexURL("foo").toString() + "]");
   }

   @View
   public void index(String param) throws IOException
   {
      if (param != null)
      {
         renderContext.getPrinter().write("1[" + A_.indexURL(null).toString() + "]");
      }
      else
      {
         renderContext.getPrinter().write("2[" + A_.indexURL().toString() + "]");
      }
   }
}
