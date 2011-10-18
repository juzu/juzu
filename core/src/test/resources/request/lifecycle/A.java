package request.lifecycle;

import org.juzu.Render;
import org.juzu.test.Registry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Render
   public void index() throws IOException
   {
      Registry.compareAndSet("count", 0, 1);
   }

   @PostConstruct
   public void after()
   {
      Registry.compareAndSet("count", null, 0);
   }

   @PreDestroy
   public void before()
   {
      Registry.compareAndSet("count", 1, 2);
   }
}
