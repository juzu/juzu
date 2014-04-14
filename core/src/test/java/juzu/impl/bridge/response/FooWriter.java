package juzu.impl.bridge.response;

import juzu.impl.request.EntityMarshaller;
import juzu.io.Chunk;
import juzu.io.ChunkBuffer;
import juzu.io.Streamable;

import java.lang.reflect.AnnotatedElement;

/**
* @author Julien Viet
*/
public class FooWriter extends EntityMarshaller {

  @Override
  public Streamable marshall(String mimeType, AnnotatedElement annotations, Object object) {
    if (mimeType.equals("text/foo") && object instanceof Foo) {
      Foo foo = (Foo)object;
      return new ChunkBuffer().append(Chunk.create(foo.data));
    } else{
      return null;
    }
  }
}
