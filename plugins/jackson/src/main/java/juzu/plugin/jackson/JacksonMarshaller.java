/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package juzu.plugin.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import juzu.impl.request.EntityMarshaller;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.io.Streamable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;

/**
 * @author Julien Viet
 */
public class JacksonMarshaller extends EntityMarshaller {

  @Override
  public Streamable marshall(String mimeType, AnnotatedElement annotations, Object object) {
    if (mimeType.equals("application/json")) {

      //
      final byte[] data;
      if (object instanceof TreeNode) {
        try {
          TreeNode tree = (TreeNode)object;
          JsonFactory jfactory = new JsonFactory();
          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          JsonGenerator generator = jfactory.createGenerator(buffer);
          ObjectMapper mapper = new ObjectMapper();
          mapper.writeTree(generator, tree);
          data = buffer.toByteArray();
        }
        catch (IOException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
      } else if (annotations.getAnnotation(Jackson.class) != null) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          mapper.writeValue(buffer, object);
          data = buffer.toByteArray();
        }
        catch (IOException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
      } else {
        data = null;
      }

      //
      if (data != null) {
        return new Streamable() {
          @Override
          public void send(Stream stream) throws IllegalStateException {
            try {
              stream.provide(Chunk.create(data));
            }
            finally {
              stream.close(null);
            }
          }
        };
      }
    }

    //
    return null;
  }
}
