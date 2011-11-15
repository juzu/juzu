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

package examples.tutorial.injection;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MovieFinderImpl implements MovieFinder
{

   private List<Movie> movies = Arrays.asList(
      new Movie("Blue Velvet", "David Lynch"),
      new Movie("Magnolia", "Paul T Anderson"),
      new Movie("Cabin Fever", "Eli Roth"),
      new Movie("In The Company Of Men", "Neil Labute"),
      new Movie("Naked", "Mike Leigh"),
      new Movie("La vita e bella", "Roberto Benigni"),
      new Movie("La vita e bella 2", "Roberto Benigni"));

   public List<Movie> findAll()
   {
      return movies;
   }
}
