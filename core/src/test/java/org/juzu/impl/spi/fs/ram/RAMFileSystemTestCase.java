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

package org.juzu.impl.spi.fs.ram;

import org.juzu.test.AbstractTestCase;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFileSystemTestCase extends AbstractTestCase
{

   public void testLastModified() throws IOException
   {
      RAMFileSystem fs = new RAMFileSystem();
      RAMFile fooTxt = fs.addFile(fs.getRoot(), "foo.txt").update("abc");
      long now = waitForOneMillis();
      assertTrue(fs.getLastModified(fooTxt) < now);
      waitForOneMillis();
      fooTxt.update("def");
      assertTrue(now < fs.getLastModified(fooTxt));
   }
}
