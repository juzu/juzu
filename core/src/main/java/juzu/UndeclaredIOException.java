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

package juzu;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * An unchecked wrapper for {@link IOException}. Note that this exception should not be abused, i.e it should not be
 * used in place of <code>IOException</code>. It should be used in the Juzu API used to develop an application, which
 * makes the <code>IOException</code> transparent to the developer, for instance we want to void the developer to care
 * about the <code>IOException</code> in this situation:
 * <p/>
 * <pre><code>
 *    public void index() throws IOException {
 *       template.render();
 *    }
 * </pre></code>
 * <p/>
 * Instead the {@link juzu.template.Template#render()} methods wraps the <code>IOException</code> with this unchecked
 * wrapper and the developer does not have to care about it anymore:
 * <p/>
 * <pre><code>
 *    public void index() {
 *       template.render();
 *    }
 * </code></pre>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class UndeclaredIOException extends UndeclaredThrowableException {

  public UndeclaredIOException(IOException undeclaredIO) {
    super(undeclaredIO);
  }
}
