package org.juzu.io;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface BinaryStream extends Stream
{
   
   BinaryStream append(byte[] data) throws IOException;
   
   BinaryStream append(byte[] data, int off, int len) throws IOException;

}
