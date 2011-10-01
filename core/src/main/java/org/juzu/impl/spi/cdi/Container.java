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

package org.juzu.impl.spi.cdi;

import org.juzu.impl.spi.fs.ReadFileSystem;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Container
{

   /** . */
   private List<ReadFileSystem<?>> fileSystems;

   protected Container()
   {
      this.fileSystems = new ArrayList<ReadFileSystem<?>>();
   }

   public abstract BeanManager getManager();

   public abstract ClassLoader getClassLoader();

   public void addFileSystem(ReadFileSystem<?> fileSystem)
   {
      fileSystems.add(fileSystem);
   }

   protected abstract void doStart(List<ReadFileSystem<?>> fileSystems) throws Exception;

   protected abstract void doStop();

   public void start() throws Exception
   {
      doStart(fileSystems);
   }

   public void stop()
   {
      doStop();
   }
}
