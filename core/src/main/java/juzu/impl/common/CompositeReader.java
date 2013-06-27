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

package juzu.impl.common;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CompositeReader extends Reader {

    /** . */
    private Iterator<Reader> compounds;

    /** . */
    private Reader current;

    public CompositeReader(Reader... compounds) throws NullPointerException {
        this(Tools.iterator(compounds));
    }

    public CompositeReader(Iterable<Reader> compounds) throws NullPointerException {
        this(compounds.iterator());
    }

    public CompositeReader(Iterator<Reader> compounds) throws NullPointerException {
        this.compounds = compounds;
        this.current = null;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = 0;
        while (len > 0) {
            if (current == null) {
                if (compounds.hasNext()) {
                    current = compounds.next();
                } else {
                    if (read == 0) {
                        // Otherwise it would loop for ever
                        read = -1;
                    }
                    break;
                }
            } else {
                int tmp = current.read(cbuf, off, len);
                if (tmp == -1) {
                    Reader reader = current;
                    current = null;
                    reader.close();
                } else {
                    off += tmp;
                    len -= tmp;
                    read += tmp;
                }
            }
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        if (current != null) {
            Tools.safeClose(current);
            current = null;
        }
        while (compounds.hasNext()) {
            Tools.safeClose(compounds.next());
        }
        compounds = null;
    }
}
