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
package juzu.impl.plugin.template;

import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.processor.MainProcessor;
import juzu.test.AbstractTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompletionTestCase extends AbstractTestCase {

  @Test
  public void testComplete() {
    CompilerAssert<File, File> compiler = compiler("plugin.template.completion");
    compiler.assertCompile();
    String[][] tests = {
        { "", "bar.gtmpl", "folder/", "foo1.gtmpl", "foo2.gtmpl"},
        { "b", "bar.gtmpl"},
        { "fo", "folder/", "foo1.gtmpl", "foo2.gtmpl"},
        { "foo", "foo1.gtmpl", "foo2.gtmpl"},
        { "foo1", "foo1.gtmpl"},
        { "folder", "folder/juu1.gtmpl", "folder/juu2.gtmpl", "folder/nested/"},
        { "folder/", "folder/juu1.gtmpl", "folder/juu2.gtmpl", "folder/nested/"},
        { "folder/juu", "folder/juu1.gtmpl", "folder/juu2.gtmpl"},
        { "folder/nes", "folder/nested/"},
        { "folder/nested", "folder/nested/daa1.gtmpl", "folder/nested/daa2.gtmpl"},
    };
    final ArrayList<String> completions = new ArrayList<String>();
    final AtomicInteger count = new AtomicInteger();
    for (final String[] test : tests) {
      compiler.with(new MainProcessor() {
        @Override
        protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
          if (count.getAndIncrement() == 0) {
            ElementHandle.Field index = ElementHandle.Field.create("plugin.template.completion.A", "index");
            VariableElement indexElt = index.get(processingEnv);
            AnnotationMirror pathAnn = indexElt.getAnnotationMirrors().get(0);
            TypeElement annotationTypeElement = (TypeElement)pathAnn.getAnnotationType().asElement();
            ExecutableElement value = (ExecutableElement)annotationTypeElement.getEnclosedElements().get(0);
            for (Completion completion : getCompletions(indexElt, pathAnn, value, test[0])) {
              completions.add(completion.getValue());
            }
          }
        }
      });
      compiler.assertCompile();
      List<String> expected = Arrays.asList(test).subList(1, test.length);
      assertEquals(
          "Was expecting completion of <" + test[0] + "> to be " + expected + " instead of " + completions,
          expected,
          completions);
      count.set(0);
      completions.clear();
    }
  }
}
