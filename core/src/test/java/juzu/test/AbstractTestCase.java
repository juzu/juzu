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

package juzu.test;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import juzu.impl.common.QN;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.protocol.mock.MockApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
//@RunWith(JUnit38ClassRunner.class)
public abstract class AbstractTestCase extends Assert {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() {
  }

  /**
   * Wait for at least one millisecond, based on the current time clock.
   *
   * @return the time captured after the wait
   */
  public static long waitForOneMillis() {
    long snapshot = System.currentTimeMillis();
    while (true) {
      try {
        long now = System.currentTimeMillis();
        if (snapshot < now) {
          return now;
        }
        else {
          snapshot = now;
          Thread.sleep(1);
        }
      }
      catch (InterruptedException e) {
        AssertionFailedError afe = new AssertionFailedError("Was not expecting interruption");
        afe.initCause(e);
        throw afe;
      }
    }
  }

  public static void fail(Throwable t) {
    throw failure(t);
  }

  public static AssertionFailedError failure(Throwable t) {
    AssertionFailedError afe = new AssertionFailedError();
    afe.initCause(t);
    return afe;
  }

  public static AssertionFailedError failure(String msg, Throwable t) {
    AssertionFailedError afe = new AssertionFailedError(msg);
    afe.initCause(t);
    return afe;
  }

  public static AssertionFailedError failure(String msg) {
    return new AssertionFailedError(msg);
  }

  public static <T> T assertInstanceOf(Class<T> expectedInstance, Object o) {
    if (expectedInstance.isInstance(o)) {
      return expectedInstance.cast(o);
    }
    else {
      throw failure("Was expecting " + o + " to be an instance of " + expectedInstance.getName());
    }
  }

  public static <T> T assertNotInstanceOf(Class<?> expectedInstance, T o) {
    if (expectedInstance.isInstance(o)) {
      throw failure("Was expecting " + o + " not to be an instance of " + expectedInstance.getName());
    }
    else {
      return o;
    }
  }

  public static void assertDelete(File f) {
    if (!f.exists()) {
      throw failure("Was expecting file " + f.getAbsolutePath() + " to exist");
    }
    if (!f.delete()) {
      throw failure("Was expecting file " + f.getAbsolutePath() + " to be deleted");
    }
  }

  public static DiskFileSystem diskFS(String... packageName) {
    File root = new File(System.getProperty("test.resources"));
    return new DiskFileSystem(root, packageName);
  }

  @Rule
  public TestName name = new TestName();

  public final String getName() {
    return name.getMethodName();
  }

  public final CompilerAssert<File, File> compiler(String... packageName) {
    return compiler(false, packageName);
  }

  public final CompilerAssert<File, File> incrementalCompiler(String... packageName) {
    return compiler(true, packageName);
  }

  private CompilerAssert<File, File> compiler(boolean incremental, String... packageName) {
    DiskFileSystem input = diskFS(packageName);

    if (packageName.length == 0) {
      throw failure("Cannot compile empty package");
    }

    //
    String outputPath = System.getProperty("test.generated.classes");
    File a = new File(outputPath);
    if (a.exists()) {
      if (a.isFile()) {
        throw failure("File " + outputPath + " already exist and is a file");
      }
    }
    else {
      if (!a.mkdirs()) {
        throw failure("Could not create test generated source directory " + outputPath);
      }
    }

    // Find
    String s = name.getMethodName();
    String pkg = Tools.join('.', packageName) + "#" + s;
    File f2 = new File(a, pkg);
    for (int count = 0;;count++) {
      if (!f2.exists()) {
        break;
      }
      else {
        f2 = new File(a, pkg + "-" + count);
      }
    }

    //
    if (!f2.mkdirs()) {
      throw failure("Could not create test generated source directory " + f2.getAbsolutePath());
    }

    //
    File sourceOutputDir = new File(f2, "source-output");
    assertTrue(sourceOutputDir.mkdir());
    DiskFileSystem sourceOutput = new DiskFileSystem(sourceOutputDir);

    //
    File classOutputDir = new File(f2, "class-output");
    assertTrue(classOutputDir.mkdir());
    DiskFileSystem classOutput = new DiskFileSystem(classOutputDir);

    //
    File sourcePathDir = new File(f2, "source-path");
    assertTrue(sourcePathDir.mkdir());
    DiskFileSystem sourcePath = new DiskFileSystem(sourcePathDir);
    try {
      input.copy(sourcePath);
    }
    catch (IOException e) {
      throw failure(e);
    }

    //
    return new CompilerAssert<File, File>(incremental, sourcePath, sourceOutput, classOutput);
  }

  public MockApplication<?> application(InjectImplementation injectImplementation, String... packageName) {
    CompilerAssert<File, File> helper = compiler(packageName);
    helper.assertCompile();
    return helper.application(injectImplementation, QN.create(packageName));
  }

  public static void assertEquals(JSON expected, JSON test) {
    if (expected != null) {
      if (test == null) {
        throw failure("Was expected " + expected + " to be not null");
      }
      else if (!equalsIgnoreNull(expected, test)) {
        StringBuilder sb;
        try {
          sb = new StringBuilder("expected <");
          expected.toString(sb, 2);
          sb.append(">  but was:<");
          test.toString(sb, 2);
          sb.append(">");
          throw failure(sb.toString());
        }
        catch (IOException e) {
          throw failure("Unexpected", e);
        }
      }
    }
    else {
      if (test != null) {
        throw failure("Was expected " + test + " to be null");
      }
    }
  }

  private static boolean equalsIgnoreNull(Object o1, Object o2) {
    if (o1 == null || o2 == null) {
      return true;
    } else if (o1 instanceof List && o2 instanceof List) {
      Iterator i1 = ((List)o1).iterator();
      Iterator i2 = ((List)o2).iterator();
      while (true) {
        boolean n1 = i1.hasNext();
        boolean n2 = i2.hasNext();
        if (n1 && n2) {
          if (!equalsIgnoreNull(i1.next(), i2.next())) {
            return false;
          }
        } else {
          return n1 == n2;
        }
      }
    } else {
      if (o1 instanceof JSON && o2 instanceof JSON) {
        JSON js1 = (JSON)o1;
        JSON js2 = (JSON)o2;
        HashSet<String> names = new HashSet<String>(js1.names());
        names.addAll(js2.names());
        for (String name : names) {
          js1.getArray("", Object.class);
          Object v1 = js1.get(name);
          Object v2 = js2.get(name);
          if (!equalsIgnoreNull(v1, v2)) {
            return false;
          }
        }
        return true;
      } else {
        return o1.equals(o2);
      }
    }
  }

  public static void assertNoSuchElement(Iterator<?> iterator) {
    try {
      Object next = iterator.next();
      fail("Was not expecting to obtain " + next + " element from an iterator");
    }
    catch (NoSuchElementException expected) {
    }
  }

  public static <E> void assertEquals(List<? extends E> expected, Iterable<? extends E> test) {
    int index = 0;
    Iterator<? extends E> expectedIterator = expected.iterator();
    Iterator<? extends E> testIterator = test.iterator();
    while (true) {
      if (expectedIterator.hasNext()) {
        if (testIterator.hasNext()) {
          E expectedNext = expectedIterator.next();
          E testNext = testIterator.next();
          if (!Tools.safeEquals(expectedNext, testNext)) {
            throw failure("Elements at index " + index + " are not equals: " + expectedNext + "!=" + testNext);
          }
          else {
            index++;
          }
        }
        else {
          throw failure("Tested iterable has more elements than the expected iterable at index " + index);
        }
      }
      else {
        if (testIterator.hasNext()) {
          throw failure("Expected iterable has more elements than the tested iterable at index " + index);
        }
        else {
          break;
        }
      }
    }
  }
}
