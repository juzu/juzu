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

package juzu.impl.common;

import juzu.io.UndeclaredIOException;
import juzu.request.ResponseParameter;
import juzu.impl.bridge.Parameters;
import org.w3c.dom.*;

import javax.annotation.processing.Completion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Tools {

  /** . */
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** . */
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

  /** . */
  public static final Charset ISO_8859_2 = Charset.forName("ISO-8859-2");

  /** . */
  public static final Charset UTF_8 = Charset.forName("UTF-8");

  /** . */
  private static final Iterator EMPTY_ITERATOR = new Iterator() {
    public boolean hasNext() {
      return false;
    }

    public Object next() {
      throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  };

  /** . */
  public static final Comparator<Completion> COMPLETION_COMPARATOR = new Comparator<Completion>() {
    public int compare(Completion o1, Completion o2) {
      return o1.getValue().compareTo(o2.getValue());
    }
  };

  /** . */
  private static final Iterable EMPTY_ITERABLE = new Iterable() {
    public Iterator iterator() {
      return EMPTY_ITERATOR;
    }
  };

  /** . */
  public static Pattern EMPTY_NO_RECURSE = Pattern.compile("");

  /** . */
  public static Pattern EMPTY_RECURSE = Pattern.compile(".*");

  public static Pattern getPackageMatcher(String packageName, boolean recurse) {

    // PackageName       -> Identifier
    // PackageName       -> PackageName . Identifier
    // Identifier        -> IdentifierChars but not a Keyword or BooleanLiteral or NullLiteral
    // IdentifierChars   -> JavaLetter
    // IdentifierChars   -> IdentifierChars JavaLetterOrDigit
    // JavaLetter        -> any Unicode character that is a Java letter
    // JavaLetterOrDigit -> any Unicode character that is a Java letter-or-digit

    if (packageName.length() == 0) {
      return recurse ? EMPTY_RECURSE : EMPTY_NO_RECURSE;
    }
    else {
      String regex;
      if (recurse) {
        regex = Pattern.quote(packageName) + "(\\..*)?";
      }
      else {
        regex = Pattern.quote(packageName);
      }
      return Pattern.compile(regex);
    }
  }

  /**
   * Returns the parent package of the provided package. Null is returned if the provided package
   * was a top level package.
   *
   * @param pkgName the package name
   * @return the parent package
   */
  public static String parentPackageOf(String pkgName) {
    int index = pkgName.lastIndexOf('.');
    if (index == -1) {
      return null;
    } else {
      return pkgName.substring(0, index);
    }
  }

  public static Class<?> getPackageClass(ClassLoader loader, String pkgName) {
    try {
      return loader.loadClass(pkgName + ".package-info");
    }
    catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static void escape(CharSequence s, StringBuilder appendable) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      if (c == '\n') {
        appendable.append("\\n");
      }
      else if (c == '\'') {
        appendable.append("\\\'");
      }
      else if (c == '\r') {
        // Skip carriage return
      } else {
        appendable.append(c);
      }
    }
  }

  public static boolean safeEquals(Object o1, Object o2) {
    return o1 == null ? o2 == null : (o2 != null && o1.equals(o2));
  }

  public static void safeClose(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (IOException ignore) {
      }
    }
  }

  public static void safeFlush(Flushable flushable) {
    if (flushable != null) {
      try {
        flushable.flush();
      }
      catch (IOException ignore) {
      }
    }
  }

  public static Method safeGetMethod(Class<?> type, String name, Class<?>... parameterTypes) {
    try {
      return type.getDeclaredMethod(name, parameterTypes);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static <C extends Collection<? super E>, E> C addAll(C collection, Iterable<E> elements) {
    for (E element : elements) {
      collection.add(element);
    }
    return collection;
  }

  public static <T> List<T> safeUnmodifiableList(T... list) {
    return safeUnmodifiableList(Arrays.asList(list));
  }

  public static <T> List<T> safeUnmodifiableList(List<T> list) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      return Collections.unmodifiableList(new ArrayList<T>(list));
    }
  }

  public static byte[] bytes(InputStream in) throws IOException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(in.available());
      copy(in, baos);
      return baos.toByteArray();
    }
    finally {
      safeClose(in);
    }
  }

  public static void write(String content, File f) throws IOException {
    write(content.getBytes(), f);
  }

  public static void write(byte[] content, File f) throws IOException {
    FileOutputStream out = new FileOutputStream(f);
    try {
      copy(new ByteArrayInputStream(content), out);
    }
    finally {
      safeClose(out);
    }
  }

  public static Map<String, String> responseHeaders(HttpURLConnection conn) {
    Map<String, String> headers = Collections.emptyMap();
    for (int i=0; ; i++) {
      String name = conn.getHeaderFieldKey(i);
      String value = conn.getHeaderField(i);
      if (name == null && value == null) {
        break;
      }
      if (name != null) {
        if (headers.isEmpty()) {
          headers = new HashMap<String, String>();
        }
        headers.put(name, value);
      }
    }
    return headers;
  }

  public static String read(URL url) throws IOException {
    return read(url.openStream());
  }

  public static String read(File f) throws IOException {
    return read(new FileInputStream(f));
  }

  public static String read(InputStream in) throws IOException {
    return read(in, Tools.UTF_8);
  }

  public static String read(InputStream in, Charset charset) throws IOException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      copy(in, baos);
      return new String(baos.toByteArray(), charset);
    }
    finally {
      safeClose(in);
    }
  }

  public static <O extends OutputStream> O copy(InputStream in, O out) throws IOException {
    byte[] buffer = new byte[256];
    for (int l;(l = in.read(buffer)) != -1;) {
      out.write(buffer, 0, l);
    }
    return out;
  }

  public static String unquote(String s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException("Can't unquote null string");
    }
    if (s.length() > 1) {
      char c1 = s.charAt(0);
      char c2 = s.charAt(s.length() - 1);
      if ((c1 == '\'' || c1 == '"') && c1 == c2) {
        return s.substring(1, s.length() - 1);
      }
    }
    return s;
  }

  public static String join(char separator, String... names) {
    return join(separator, names, 0, names.length);
  }

  public static String join(char separator, String[] names, int from, int end) {
    int length = 0;
    for (int i = from;i < end;i++) {
      if (i > from) {
        length++;
      }
      length += names[i].length();
    }
    return join(new StringBuilder(length), separator, names, from, end).toString();
  }

  public static String join(char separator, Iterator<String> names) {
    return join(new StringBuilder(), separator, names).toString();
  }

  public static String join(char separator, Iterable<String> names) {
    return join(separator, names.iterator());
  }

  public static StringBuilder join(StringBuilder sb, char separator, String... names) {
    return join(sb, separator, names, 0, names.length);
  }

  public static StringBuilder join(StringBuilder sb, char separator, String[] names, int from, int end) {
    try {
      join((Appendable)sb, separator, names, from, end);
      return sb;
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public static StringBuilder join(StringBuilder sb, char separator, Iterator<String> names) {
    try {
      join((Appendable)sb, separator, names);
      return sb;
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public static StringBuilder join(StringBuilder sb, char separator, Iterable<String> names) {
    try {
      join((Appendable)sb, separator, names);
      return sb;
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  public static <A extends Appendable> Appendable join(A appendable, char separator, String... names) throws IOException {
    return join(appendable, separator, names, 0, names.length);
  }

  public static <A extends Appendable> Appendable join(A appendable, char separator, String[] names, int from, int end) throws IOException {
    int length = end - from;
    switch (length) {
      case 0:
        break;
      case 1:
        appendable.append(names[from]);
        break;
      default:
        for (int i = from;i < end;i++) {
          if (i > from) {
            appendable.append(separator);
          }
          appendable.append(names[i]);
        }
        break;
    }
    return appendable;
  }

  public static <A extends Appendable> Appendable join(A appendable, char separator, Iterable<String> names) throws IOException {
    return join(appendable, separator, names.iterator());
  }

  public static <A extends Appendable> Appendable join(A appendable, char separator, Iterator<String> names) throws IOException {
    if (names.hasNext()) {
      appendable.append(names.next());
      while (names.hasNext()) {
        appendable.append(separator);
        appendable.append(names.next());
      }
    }
    return appendable;
  }

  public static <A extends Appendable> A nameOf(Class<?> clazz, A appendable) throws IOException {
    if (clazz.isMemberClass()) {
      nameOf(clazz.getEnclosingClass(), appendable).append('.').append(clazz.getSimpleName());
    } else {
      appendable.append(clazz.getSimpleName());
    }
    return appendable;
  }

  public static String getName(Class<?> clazz) {
    if (clazz.isLocalClass() || clazz.isAnonymousClass()) {
      throw new IllegalArgumentException("Cannot use local or anonymous class");
    }
    else {
      try {
        return nameOf(clazz, new StringBuilder()).toString();
      }
      catch (IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  public static String getImport(Class<?> clazz) {
    if (clazz.isLocalClass() || clazz.isAnonymousClass()) {
      throw new IllegalArgumentException("Cannot use local or anonymous class");
    }
    else if (clazz.isMemberClass()) {
      StringBuilder sb = new StringBuilder();
      while (clazz.isMemberClass()) {
        sb.insert(0, clazz.getSimpleName());
        sb.insert(0, '.');
        clazz = clazz.getEnclosingClass();
      }
      sb.insert(0, clazz.getSimpleName());
      String pkg = clazz.getPackage().getName();
      if (pkg.length() > 0) {
        sb.insert(0, '.');
        sb.insert(0, pkg);
      }
      return sb.toString();
    }
    else {
      return clazz.getName();
    }
  }

  /**
   * <p>Add the specified to the specified set and returns the result. When the <code>set</code> argument is an
   * instance of {@link HashSet} the element is directly added, otherwise a new <code>HashSet</code> is created by
   * cloning the <code>set</code> argument and the <code>e</code> argument is added.</p>
   * <p/>
   * <p>Usage pattern : adding a set to a non modifiable set</p>
   * <pre><code>
   *    Set&lt;String&gt; set = Collections.emptySet();
   *    set = addToHashSet(set, "foo");
   * </code></pre>
   *
   * @param set the set
   * @param e   the element
   * @param <E> the set generic type
   * @return an <code>HashSet</code> containing the element
   */
  public static <E> HashSet<E> addToHashSet(Set<E> set, E e) {
    HashSet<E> hashSet;
    if (set instanceof HashSet) {
      hashSet = (HashSet<E>)set;
    }
    else {
      hashSet = new HashSet<E>(set);
    }
    hashSet.add(e);
    return hashSet;
  }

  /**
   * <p>Add the specified to the specified list and returns the result. When the <code>list</code> argument is an
   * instance of {@link ArrayList} the element is directly added, otherwise a new <code>ArrayList</code> is created by
   * cloning the <code>list</code> argument and the <code>e</code> argument is added.</p>
   * <p/>
   * <p>Usage pattern : adding a list to a non modifiable list</p>
   * <pre><code>
   *    List&lt;String&gt; list = Collections.emptyList();
   *    list = addToArrayList(list, "foo");
   * </code></pre>
   *
   * @param list the list
   * @param e    the element
   * @param <E>  the set generic type
   * @return an <code>ArrayList</code> containing the element
   */
  public static <E> ArrayList<E> addToArrayList(List<E> list, E e) {
    ArrayList<E> arrayList;
    if (list instanceof ArrayList) {
      arrayList = (ArrayList<E>)list;
    }
    else {
      arrayList = new ArrayList<E>(list);
    }
    arrayList.add(e);
    return arrayList;
  }

  public static <E> HashSet<E> set() {
    return new HashSet<E>();
  }

  public static <E> HashSet<E> set(E element) {
    HashSet<E> set = new HashSet<E>();
    set.add(element);
    return set;
  }

  public static <E> HashSet<E> set(E... elements) {
    HashSet<E> set = new HashSet<E>(elements.length);
    Collections.addAll(set, elements);
    return set;
  }

  public static <E> HashSet<E> set(Iterable<E> elements) {
    return set(elements.iterator());
  }

  public static <E> HashSet<E> set(Iterator<E> elements) {
    HashSet<E> list = new HashSet<E>();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }

  public static <E> HashSet<E> set(Enumeration<E> elements) {
    HashSet<E> list = new HashSet<E>();
    while (elements.hasMoreElements()) {
      list.add(elements.nextElement());
    }
    return list;
  }

  public static <E> ArrayList<E> list(Iterable<E> elements) {
    return list(elements.iterator());
  }

  public static <E> ArrayList<E> list(Iterator<E> elements) {
    ArrayList<E> list = new ArrayList<E>();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }

  public static <E> ArrayList<E> list(Enumeration<E> elements) {
    ArrayList<E> list = new ArrayList<E>();
    while (elements.hasMoreElements()) {
      list.add(elements.nextElement());
    }
    return list;
  }

  public static <E> ArrayList<E> list(E... elements) {
    ArrayList<E> set = new ArrayList<E>(elements.length);
    Collections.addAll(set, elements);
    return set;
  }


  public static <E> Iterable<E> iterable(final Enumeration<E> elements) throws NullPointerException {
    return new Iterable<E>() {
      public Iterator<E> iterator() {
        return Tools.iterator(elements);
      }
    };
  }

  public static <E> Iterator<E> iterator(final Enumeration<E> elements) throws NullPointerException {
    return new Iterator<E>() {
      public boolean hasNext() {
        return elements.hasMoreElements();
      }
      public E next() {
        return elements.nextElement();
      }
      public void remove() {
        throw new UnsupportedOperationException("Read only");
      }
    };
  }

  public static <E> Iterable<E> iterable(final E element) throws NullPointerException {
    return new Iterable<E>() {
      public Iterator<E> iterator() {
        return Tools.iterator(element);
      }
    };
  }

  public static <E> Iterator<E> iterator(final E element) throws NullPointerException {
    return new Iterator<E>() {
      boolean hasNext = true;
      public boolean hasNext() {
        return hasNext;
      }
      public E next() {
        if (hasNext) {
          hasNext = false;
          return element;
        } else {
          throw new NoSuchElementException();
        }
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static class ArrayIterator<E> implements Iterator<E> {

    /** . */
    private final E[] elements;

    /** . */
    private final int to;

    /** . */
    private int current;

    ArrayIterator(E[] elements, int to, int current) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
      if (elements == null) {
        throw new NullPointerException("No null elements accepted");
      }
      if (current < 0) {
        throw new IndexOutOfBoundsException("From index cannot be negative");
      }
      if (to > elements.length + 1) {
        throw new IndexOutOfBoundsException("To index cannot be greater than the array size + 1");
      }
      if (current > to) {
        throw new IllegalArgumentException("From index cannot be greater than the to index");
      }

      //
      this.elements = elements;
      this.current = current;
      this.to = to;
    }

    public boolean hasNext() {
      return current < to;
    }

    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return elements[current++];
    }

    public void remove() {
      throw new NoSuchElementException();
    }
  }

  public static class IterableArray<E> implements Iterable<E> {

    /** . */
    private final E[] elements;

    /** . */
    private final int from;

    /** . */
    private final int to;

    IterableArray(E[] elements, int from, int to) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
      if (elements == null) {
        throw new NullPointerException("No null elements accepted");
      }
      if (from < 0) {
        throw new IndexOutOfBoundsException("From index cannot be negative");
      }
      if (to > elements.length + 1) {
        throw new IndexOutOfBoundsException("To index cannot be greater than the array size + 1");
      }
      if (from > to) {
        throw new IllegalArgumentException("From index cannot be greater than the to index");
      }

      //
      this.elements = elements;
      this.from = from;
      this.to = to;
    }

    public Iterator<E> iterator() {
      return new ArrayIterator<E>(elements, to, from);
    }
  }

  public static <E> Iterable<E> iterable(final E... elements) throws NullPointerException {
    return new IterableArray<E>(elements, 0, elements.length);
  }

  public static <E> Iterator<E> iterator(E... elements) throws NullPointerException {
    return new ArrayIterator<E>(elements, elements.length, 0);
  }

  /**
   * Create an iterable from the array and bounds
   *
   * @param elements the elements to wrap
   * @param from the from bound
   * @param to the to bound
   * @param <E> the element generic type
   * @return the iterable
   * @throws NullPointerException if the array is null
   * @throws IndexOutOfBoundsException when the bounds are outside of the array
   * @throws IllegalArgumentException if the from argument is greater than the to index
   */
  public static <E> IterableArray<E> iterable(E[] elements, int from, int to) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
    return new IterableArray<E>(elements, from, to);
  }

  public static <E> Iterator<E> iterator(int from, final E... elements) throws NullPointerException, IndexOutOfBoundsException {
    return new ArrayIterator<E>(elements, elements.length, from);
  }

  public static <E> Iterable<E> iterable(final int from, final int to, final E... elements) throws NullPointerException, IndexOutOfBoundsException {
    return new IterableArray<E>(elements, from, to);
  }

  public static <E> Iterator<E> iterator(final int from, final int to, final E... elements) throws NullPointerException, IndexOutOfBoundsException {
    return new ArrayIterator<E>(elements, to, from);
  }

  public static <E> Iterator<E> emptyIterator() {
    @SuppressWarnings("unchecked")
    Iterator<E> iterator = EMPTY_ITERATOR;
    return iterator;
  }

  public static <E> Iterable<E> emptyIterable() {
    @SuppressWarnings("unchecked")
    Iterable<E> iterable = EMPTY_ITERABLE;
    return iterable;
  }

  public static <E> Iterator<E> append(final Iterator<E> iterator, final E... elements) {
    return new Iterator<E>() {

      /** -1 means the iterator should be used, otherwise it's the index. */
      int index = -1;

      public boolean hasNext() {
        if (index == -1) {
          if (iterator.hasNext()) {
            return true;
          }
          else {
            index = 0;
          }
        }
        return index < elements.length;
      }

      public E next() {
        if (index == -1) {
          if (iterator.hasNext()) {
            return iterator.next();
          }
          else {
            index = 0;
          }
        }
        if (index < elements.length) {
          return elements[index++];
        }
        else {
          throw new NoSuchElementException();
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Append an object to an array of objects. The original array is not modified. The returned array will be of the
   * same component type of the provided array and its first n elements where n is the size of the provided array will
   * be the elements of the provided array. The last element of the array will be the provided object to append.
   *
   * @param array the array to augment
   * @param o     the object to append
   * @return a new array
   * @throws IllegalArgumentException if the array is null
   * @throws ClassCastException       if the appended object class prevents it from being added to the array
   */
  public static <E> E[] appendTo(E[] array, E o) throws IllegalArgumentException, ClassCastException
  {
    if (array == null)
    {
      throw new IllegalArgumentException();
    }

    //
    Class componentType = array.getClass().getComponentType();
    if (o != null && !componentType.isAssignableFrom(o.getClass()))
    {
      throw new ClassCastException("Object with class " + o.getClass().getName() + " cannot be casted to class " + componentType.getName());
    }

    //
    E[] copy = (E[])Array.newInstance(componentType, array.length + 1);
    System.arraycopy(array, 0, copy, 0, array.length);
    copy[array.length] = o;

    //
    return copy;
  }

  public static <S extends Serializable> S unserialize(Class<S> expectedType, File f) throws IOException, ClassNotFoundException {
    return unserialize(expectedType, new FileInputStream(f));
  }

  public static <S> S unserialize(Class<S> expectedType, InputStream in) throws IOException, ClassNotFoundException {
    return unserialize(null, expectedType, in);
  }

  public static <S> S unserialize(final ClassLoader loader, Class<S> expectedType, InputStream in) throws IOException, ClassNotFoundException {
    try {
      ObjectInputStream ois = new ObjectInputStream(in) {
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          if (loader != null) {
            return Class.forName(desc.getName(), true, loader);
          } else {
            return super.resolveClass(desc);
          }
        }
      };
      Object o = ois.readObject();
      return expectedType.cast(o);
    }
    finally {
      safeClose(in);
    }
  }

  public static <S extends Serializable> void serialize(S value, File f) throws IOException {
    serialize(value, new FileOutputStream(f));
  }


  public static <T extends Serializable> void serialize(T value, OutputStream out) throws IOException {
    ObjectOutputStream ois = new ObjectOutputStream(out);
    try {
      ois.writeObject(value);
    }
    finally {
      safeClose(out);
    }
  }

  public static <T extends Serializable> T clone(T value) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Tools.serialize(value, baos);
    byte[] bytes = baos.toByteArray();
    return (T)unserialize(Object.class, new ByteArrayInputStream(bytes));
  }

  public static int unsignedByteToInt(byte b) {
    return (int)b & 0xFF;
  }

  /**
   * Parses a date formatted as ISO 8601.
   *
   * @param date the date
   * @return the time in millis corresponding to the date
   */
  public static long parseISO8601(String date) {
    return DatatypeConverter.parseDateTime(date).getTimeInMillis();
  }

  /**
   * Format the time millis as an ISO 8601 date.
   *
   * @param timeMillis the time to format
   * @return the ISO 8601 corresponding dat
   */
  public static String formatISO8601(long timeMillis) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(timeMillis);
    return DatatypeConverter.printDateTime(c);
  }

  public static long handle(Element te) {
    long hash = 0;
    for (Element enclosed : te.getEnclosedElements()) {
      hash = 31 * hash + handle(enclosed);
    }
    hash = 31 * hash + te.getSimpleName().toString().hashCode();
    return hash;
  }

  public static int indexOf(CharSequence s, char c, int from) {
    return indexOf(s, c, from, s.length());
  }

  public static int indexOf(CharSequence s, char c, int from, int end) {
    if (from < end) {
      if (from < 0) {
        from = 0;
      }
      while (from < end) {
        if (s.charAt(from) == c) {
          return from;
        }
        from++;
      }
    }
    return -1;
  }

  public static int lastIndexOf(CharSequence s, char c) {
    return lastIndexOf(s, c, s.length() - 1);
  }

  public static int lastIndexOf(CharSequence s, char c, int from) {
    from = Math.min(from, s.length() - 1);
    while (from >= 0) {
      if (s.charAt(from) == c) {
        return from;
      } else {
        from--;
      }
    }
    return -1;
  }

  /**
   * Count the occurence of the separator string in the specified string with no overlapping.
   *
   * @param s the string to count in
   * @param separator the separator
   * @return the number of occurence
   */
  public static int count(String s, String separator) {
    if (separator.length() == 0) {
      return s.length() + 1;
    } else {
      int count = 0;
      int prev = 0;
      while (true) {
        int pos = s.indexOf(separator, prev);
        if (pos == -1) {
          break;
        } else {
          count++;
          prev = pos + separator.length();
        }
      }
      return count;
    }
  }

  public static String[] split(CharSequence s, char separator) {
    return foo(s, separator, 0, 0, 0);
  }

  public static String[] split(CharSequence s, char separator, int rightPadding) {
    if (rightPadding < 0) {
      throw new IllegalArgumentException("Right padding cannot be negative");
    }
    return foo(s, separator, 0, 0, rightPadding);
  }

  private static String[] foo(CharSequence s, char separator, int count, int from, int rightPadding) {
    int len = s.length();
    if (from < len) {
      int to = from;
      while (to < len && s.charAt(to) != separator) {
        to++;
      }
      String[] ret;
      if (to == len - 1) {
        ret = new String[count + 2 + rightPadding];
        ret[count + 1] = "";
      }
      else {
        ret = to == len ? new String[count + 1 + rightPadding] : foo(s, separator, count + 1, to + 1, rightPadding);
      }
      ret[count] = from == to ? "" : s.subSequence(from, to).toString();
      return ret;
    }
    else if (from == len) {
      return new String[count + rightPadding];
    }
    else {
      throw new AssertionError();
    }
  }

  public static AnnotationMirror getAnnotation(Element element, String annotationFQN) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().toString().equals(annotationFQN)) {
        return mirror;
      }
    }
    return null;
  }

  public static StringBuilder toString(Iterable<Map.Entry<String, String[]>> entries, StringBuilder sb) {
    sb.append('{');
    for (Iterator<Map.Entry<String, String[]>> i = entries.iterator();i.hasNext();) {
      Map.Entry<String, String[]> entry = i.next();
      sb.append(entry.getKey()).append("=[");
      String[] value = entry.getValue();
      for (int j = 0;j < value.length;j++) {
        if (j > 0) {
          sb.append(',');
        }
        sb.append(value[j]);
      }
      sb.append(']');
      if (i.hasNext()) {
        sb.append(',');
      }
    }
    sb.append('}');
    return sb;
  }

  public static String nextUUID() {
    return UUID.randomUUID().toString();
  }

  public static HashMap<String, String[]> toHashMap(Parameters parameters) {
    HashMap<String, String[]> map = new HashMap<String, String[]>();
    for (ResponseParameter parameter : parameters.values()) {
      map.put(parameter.getName(), parameter.toArray());
    }
    return map;
  }

  public static BigInteger bitSet(CharSequence s) {
    BigInteger current = BigInteger.ZERO;
    for (int i = s.length() - 1;i >= 0;i--) {
      char c = s.charAt(i);
      current = current.setBit(c);

    }
    return current;
  }

  public static BigInteger bitSet(char... chars) {
    BigInteger current = BigInteger.ZERO;
    for (char c : chars) {
      current = current.setBit(c);
    }
    return current;
  }

  public static String[] safeConcat(String[] first, String[] second) {
    if (first != null) {
      if (second != null) {
        String[] concat = new String[first.length + second.length];
        System.arraycopy(first, 0, concat, 0, first.length);
        System.arraycopy(second, 0, concat, first.length, second.length);
        return concat;
      } else {
        return first;
      }
    } else {
      if (second != null) {
        return second;
      } else {
        return EMPTY_STRING_ARRAY;
      }
    }
  }

  /**
   * Returns the cause of the argument or the argument itself when its cause is null. So this method
   * should never return null.
   *
   * @param throwable the throwable
   * @return the cause
   * @throws NullPointerException if the throwable argument is null
   */
  public static Throwable safeCause(Throwable throwable) throws NullPointerException {
    Throwable cause = throwable.getCause();
    return cause != null ? cause : throwable;
  }

  /** Char to entity, we use 256x256 instead of 65536 for compressing space. */
  private static final String[][] HTML_ESCAPES = new String[256][];

  private static void addNamedHtmlEntity(int c, String entity) {
    addHtmlEscape(c, "&" + entity + ";");
  }

  private static void addHtmlEscape(int c, String entity) {
    int i1 = c >> 8;
    String[] table = HTML_ESCAPES[i1];
    if (table == null) {
      HTML_ESCAPES[i1] = table = new String[256];
    }
    table[c & 0xFF] = entity;
  }

  /**
   * Lookup an html escape for the specified char.
   *
   * @param c the char to lookup
   * @return the corresponding html escape or null if it does not exist
   */
  public static String getHtmlEscape(char c) {
    int lookup = (c >> 8);
    if (lookup < 256) {
      String[] table = HTML_ESCAPES[lookup];
      int pointer = c & 0xFF;
      return table[pointer];
    }
    return null;
  }

  static {

    // Common html entities
    addNamedHtmlEntity(160, "nbsp");
    addNamedHtmlEntity(161, "iexcl");
    addNamedHtmlEntity(162, "cent");
    addNamedHtmlEntity(163, "pound");
    addNamedHtmlEntity(164, "curren");
    addNamedHtmlEntity(165, "yen");
    addNamedHtmlEntity(166, "brvbar");
    addNamedHtmlEntity(167, "sect");
    addNamedHtmlEntity(168, "uml");
    addNamedHtmlEntity(169, "copy");
    addNamedHtmlEntity(170, "ordf");
    addNamedHtmlEntity(171, "laquo");
    addNamedHtmlEntity(172, "not");
    addNamedHtmlEntity(173, "shy");
    addNamedHtmlEntity(174, "reg");
    addNamedHtmlEntity(175, "macr");
    addNamedHtmlEntity(176, "deg");
    addNamedHtmlEntity(177, "plusmn");
    addNamedHtmlEntity(178, "sup2");
    addNamedHtmlEntity(179, "sup3");
    addNamedHtmlEntity(180, "acute");
    addNamedHtmlEntity(181, "micro");
    addNamedHtmlEntity(182, "para");
    addNamedHtmlEntity(183, "middot");
    addNamedHtmlEntity(184, "cedil");
    addNamedHtmlEntity(185, "sup1");
    addNamedHtmlEntity(186, "ordm");
    addNamedHtmlEntity(187, "raquo");
    addNamedHtmlEntity(188, "frac14");
    addNamedHtmlEntity(189, "frac12");
    addNamedHtmlEntity(190, "frac34");
    addNamedHtmlEntity(191, "iquest");
    addNamedHtmlEntity(192, "Agrave");
    addNamedHtmlEntity(193, "Aacute");
    addNamedHtmlEntity(194, "Acirc");
    addNamedHtmlEntity(195, "Atilde");
    addNamedHtmlEntity(196, "Auml");
    addNamedHtmlEntity(197, "Aring");
    addNamedHtmlEntity(198, "AElig");
    addNamedHtmlEntity(199, "Ccedil");
    addNamedHtmlEntity(200, "Egrave");
    addNamedHtmlEntity(201, "Eacute");
    addNamedHtmlEntity(202, "Ecirc");
    addNamedHtmlEntity(203, "Euml");
    addNamedHtmlEntity(204, "Igrave");
    addNamedHtmlEntity(205, "Iacute");
    addNamedHtmlEntity(206, "Icirc");
    addNamedHtmlEntity(207, "Iuml");
    addNamedHtmlEntity(208, "ETH");
    addNamedHtmlEntity(209, "Ntilde");
    addNamedHtmlEntity(210, "Ograve");
    addNamedHtmlEntity(211, "Oacute");
    addNamedHtmlEntity(212, "Ocirc");
    addNamedHtmlEntity(213, "Otilde");
    addNamedHtmlEntity(214, "Ouml");
    addNamedHtmlEntity(215, "times");
    addNamedHtmlEntity(216, "Oslash");
    addNamedHtmlEntity(217, "Ugrave");
    addNamedHtmlEntity(218, "Uacute");
    addNamedHtmlEntity(219, "Ucirc");
    addNamedHtmlEntity(220, "Uuml");
    addNamedHtmlEntity(221, "Yacute");
    addNamedHtmlEntity(222, "THORN");
    addNamedHtmlEntity(223, "szlig");
    addNamedHtmlEntity(224, "agrave");
    addNamedHtmlEntity(225, "aacute");
    addNamedHtmlEntity(226, "acirc");
    addNamedHtmlEntity(227, "atilde");
    addNamedHtmlEntity(228, "auml");
    addNamedHtmlEntity(229, "aring");
    addNamedHtmlEntity(230, "aelig");
    addNamedHtmlEntity(231, "ccedil");
    addNamedHtmlEntity(232, "egrave");
    addNamedHtmlEntity(233, "eacute");
    addNamedHtmlEntity(234, "ecirc");
    addNamedHtmlEntity(235, "euml");
    addNamedHtmlEntity(236, "igrave");
    addNamedHtmlEntity(237, "iacute");
    addNamedHtmlEntity(238, "icirc");
    addNamedHtmlEntity(239, "iuml");
    addNamedHtmlEntity(240, "eth");
    addNamedHtmlEntity(241, "ntilde");
    addNamedHtmlEntity(242, "ograve");
    addNamedHtmlEntity(243, "oacute");
    addNamedHtmlEntity(244, "ocirc");
    addNamedHtmlEntity(245, "otilde");
    addNamedHtmlEntity(246, "ouml");
    addNamedHtmlEntity(247, "divide");
    addNamedHtmlEntity(248, "oslash");
    addNamedHtmlEntity(249, "ugrave");
    addNamedHtmlEntity(250, "uacute");
    addNamedHtmlEntity(251, "ucirc");
    addNamedHtmlEntity(252, "uuml");
    addNamedHtmlEntity(253, "yacute");
    addNamedHtmlEntity(254, "thorn");
    addNamedHtmlEntity(255, "yuml");
    addNamedHtmlEntity(402, "fnof");
    addNamedHtmlEntity(913, "Alpha");
    addNamedHtmlEntity(914, "Beta");
    addNamedHtmlEntity(915, "Gamma");
    addNamedHtmlEntity(916, "Delta");
    addNamedHtmlEntity(917, "Epsilon");
    addNamedHtmlEntity(918, "Zeta");
    addNamedHtmlEntity(919, "Eta");
    addNamedHtmlEntity(920, "Theta");
    addNamedHtmlEntity(921, "Iota");
    addNamedHtmlEntity(922, "Kappa");
    addNamedHtmlEntity(923, "Lambda");
    addNamedHtmlEntity(924, "Mu");
    addNamedHtmlEntity(925, "Nu");
    addNamedHtmlEntity(926, "Xi");
    addNamedHtmlEntity(927, "Omicron");
    addNamedHtmlEntity(928, "Pi");
    addNamedHtmlEntity(929, "Rho");
    addNamedHtmlEntity(931, "Sigma");
    addNamedHtmlEntity(932, "Tau");
    addNamedHtmlEntity(933, "Upsilon");
    addNamedHtmlEntity(934, "Phi");
    addNamedHtmlEntity(935, "Chi");
    addNamedHtmlEntity(936, "Psi");
    addNamedHtmlEntity(937, "Omega");
    addNamedHtmlEntity(945, "alpha");
    addNamedHtmlEntity(946, "beta");
    addNamedHtmlEntity(947, "gamma");
    addNamedHtmlEntity(948, "delta");
    addNamedHtmlEntity(949, "epsilon");
    addNamedHtmlEntity(950, "zeta");
    addNamedHtmlEntity(951, "eta");
    addNamedHtmlEntity(952, "theta");
    addNamedHtmlEntity(953, "iota");
    addNamedHtmlEntity(954, "kappa");
    addNamedHtmlEntity(955, "lambda");
    addNamedHtmlEntity(956, "mu");
    addNamedHtmlEntity(957, "nu");
    addNamedHtmlEntity(958, "xi");
    addNamedHtmlEntity(959, "omicron");
    addNamedHtmlEntity(960, "pi");
    addNamedHtmlEntity(961, "rho");
    addNamedHtmlEntity(962, "sigmaf");
    addNamedHtmlEntity(963, "sigma");
    addNamedHtmlEntity(964, "tau");
    addNamedHtmlEntity(965, "upsilon");
    addNamedHtmlEntity(966, "phi");
    addNamedHtmlEntity(967, "chi");
    addNamedHtmlEntity(968, "psi");
    addNamedHtmlEntity(969, "omega");
    addNamedHtmlEntity(977, "thetasym");
    addNamedHtmlEntity(978, "upsih");
    addNamedHtmlEntity(982, "piv");
    addNamedHtmlEntity(8226, "bull");
    addNamedHtmlEntity(8230, "hellip");
    addNamedHtmlEntity(8242, "prime");
    addNamedHtmlEntity(8243, "Prime");
    addNamedHtmlEntity(8254, "oline");
    addNamedHtmlEntity(8260, "frasl");
    addNamedHtmlEntity(8472, "weierp");
    addNamedHtmlEntity(8465, "image");
    addNamedHtmlEntity(8476, "real");
    addNamedHtmlEntity(8482, "trade");
    addNamedHtmlEntity(8501, "alefsym");
    addNamedHtmlEntity(8592, "larr");
    addNamedHtmlEntity(8593, "uarr");
    addNamedHtmlEntity(8594, "rarr");
    addNamedHtmlEntity(8595, "darr");
    addNamedHtmlEntity(8596, "harr");
    addNamedHtmlEntity(8629, "crarr");
    addNamedHtmlEntity(8656, "lArr");
    addNamedHtmlEntity(8657, "uArr");
    addNamedHtmlEntity(8658, "rArr");
    addNamedHtmlEntity(8659, "dArr");
    addNamedHtmlEntity(8660, "hArr");
    addNamedHtmlEntity(8704, "forall");
    addNamedHtmlEntity(8706, "part");
    addNamedHtmlEntity(8707, "exist");
    addNamedHtmlEntity(8709, "empty");
    addNamedHtmlEntity(8711, "nabla");
    addNamedHtmlEntity(8712, "isin");
    addNamedHtmlEntity(8713, "notin");
    addNamedHtmlEntity(8715, "ni");
    addNamedHtmlEntity(8719, "prod");
    addNamedHtmlEntity(8721, "sum");
    addNamedHtmlEntity(8722, "minus");
    addNamedHtmlEntity(8727, "lowast");
    addNamedHtmlEntity(8730, "radic");
    addNamedHtmlEntity(8733, "prop");
    addNamedHtmlEntity(8734, "infin");
    addNamedHtmlEntity(8736, "ang");
    addNamedHtmlEntity(8743, "and");
    addNamedHtmlEntity(8744, "or");
    addNamedHtmlEntity(8745, "cap");
    addNamedHtmlEntity(8746, "cup");
    addNamedHtmlEntity(8747, "int");
    addNamedHtmlEntity(8756, "there4");
    addNamedHtmlEntity(8764, "sim");
    addNamedHtmlEntity(8773, "cong");
    addNamedHtmlEntity(8776, "asymp");
    addNamedHtmlEntity(8800, "ne");
    addNamedHtmlEntity(8801, "equiv");
    addNamedHtmlEntity(8804, "le");
    addNamedHtmlEntity(8805, "ge");
    addNamedHtmlEntity(8834, "sub");
    addNamedHtmlEntity(8835, "sup");
    addNamedHtmlEntity(8836, "nsub");
    addNamedHtmlEntity(8838, "sube");
    addNamedHtmlEntity(8839, "supe");
    addNamedHtmlEntity(8853, "oplus");
    addNamedHtmlEntity(8855, "otimes");
    addNamedHtmlEntity(8869, "perp");
    addNamedHtmlEntity(8901, "sdot");
    addNamedHtmlEntity(8968, "lceil");
    addNamedHtmlEntity(8969, "rceil");
    addNamedHtmlEntity(8970, "lfloor");
    addNamedHtmlEntity(8971, "rfloor");
    addNamedHtmlEntity(9001, "lang");
    addNamedHtmlEntity(9002, "rang");
    addNamedHtmlEntity(9674, "loz");
    addNamedHtmlEntity(9824, "spades");
    addNamedHtmlEntity(9827, "clubs");
    addNamedHtmlEntity(9829, "hearts");
    addNamedHtmlEntity(9830, "diams");
    addNamedHtmlEntity(34, "quot");
    addNamedHtmlEntity(38, "amp");
    addNamedHtmlEntity(60, "lt");
    addNamedHtmlEntity(62, "gt");
    addNamedHtmlEntity(338, "OElig");
    addNamedHtmlEntity(339, "oelig");
    addNamedHtmlEntity(352, "Scaron");
    addNamedHtmlEntity(353, "scaron");
    addNamedHtmlEntity(376, "Yuml");
    addNamedHtmlEntity(710, "circ");
    addNamedHtmlEntity(732, "tilde");
    addNamedHtmlEntity(8194, "ensp");
    addNamedHtmlEntity(8195, "emsp");
    addNamedHtmlEntity(8201, "thinsp");
    addNamedHtmlEntity(8204, "zwnj");
    addNamedHtmlEntity(8205, "zwj");
    addNamedHtmlEntity(8206, "lrm");
    addNamedHtmlEntity(8207, "rlm");
    addNamedHtmlEntity(8211, "ndash");
    addNamedHtmlEntity(8212, "mdash");
    addNamedHtmlEntity(8216, "lsquo");
    addNamedHtmlEntity(8217, "rsquo");
    addNamedHtmlEntity(8218, "sbquo");
    addNamedHtmlEntity(8220, "ldquo");
    addNamedHtmlEntity(8221, "rdquo");
    addNamedHtmlEntity(8222, "bdquo");
    addNamedHtmlEntity(8224, "dagger");
    addNamedHtmlEntity(8225, "Dagger");
    addNamedHtmlEntity(8240, "permil");
    addNamedHtmlEntity(8249, "lsaquo");
    addNamedHtmlEntity(8250, "rsaquo");
    addNamedHtmlEntity(8364, "euro");
  }

  public static void encodeHtmlText(CharSequence src, int from, int end, Appendable dst) throws IOException, IllegalArgumentException {
    encodeHtml(src, from, end, dst);
  }

  public static void encodeHtmlAttribute(CharSequence src, int from, int end, Appendable dst) throws IOException, IllegalArgumentException {
    encodeHtml(src, from, end, dst);
  }

  private static void encodeHtml(CharSequence src, int from, int to, Appendable dst) throws IOException, IllegalArgumentException {
    if (from < 0) {
      throw new IllegalArgumentException("From bound cannot be negative");
    }
    if (to > src.length()) {
      throw new IllegalArgumentException("To bound cannot be greater than source length");
    }
    if (from > to) {
      throw new IllegalArgumentException("From bound cannot be greater than to bound");
    }
    while (from < to) {
      char c = src.charAt(from++);
      String escape = getHtmlEscape(c);
      if (escape != null) {
        dst.append(escape);
      } else {
        dst.append(c);
      }
    }
  }

  /** . */
  private static final char[] HEX = "0123456789abcdef".toCharArray();

  /** . */
  private static final int[] MASKS_AND_SHIFTS = {
      0xF0000000, 7 * 4,
      0x0F000000, 6 * 4,
      0x00F00000, 5 * 4,
      0x000F0000, 4 * 4,
      0x0000F000, 3 * 4,
      0x00000F00, 2 * 4,
      0x000000F0, 1 * 4,
      0x0000000F, 0 * 4,
  };

  public static <A extends Appendable> A toHex(int value, A appendable) throws IOException {
    boolean foo = false;
    for (int i = 0;i < MASKS_AND_SHIFTS.length;i += 2) {
      int tmp = value & MASKS_AND_SHIFTS[i];
      if (tmp != 0) {
        tmp >>>= MASKS_AND_SHIFTS[i + 1];
        char cc = HEX[tmp];
        appendable.append(cc);
        foo= true;
      } else if (foo || i == MASKS_AND_SHIFTS.length - 2) {
        appendable.append("0");
      }
    }
    return appendable;
  }

  public static <A extends Appendable> A encodeHtml(org.w3c.dom.Element element, A dst) throws IOException {
    String tagName = element.getTagName();

    // Determine if empty
    // Note that we won't accumulate the elements that would be serialized for performance reason
    // we will just reiterate later before ending the element
    boolean empty;
    if (tagName.equalsIgnoreCase("script")) {
      empty = false;
    } else {
      empty = true;
      NodeList children = element.getChildNodes();
      int length = children.getLength();
      for (int i = 0; i < length && empty; i++) {
        Node child = children.item(i);
        if (child instanceof CharacterData) {
          empty = false;
        } else if (child instanceof org.w3c.dom.Element) {
          empty = false;
        }
      }
    }

    //
    dst.append('<');
    dst.append(tagName);

    // Write attributes
    if (element.hasAttributes()) {
      NamedNodeMap attrs = element.getAttributes();
      int length = attrs.getLength();
      for (int i = 0; i < length; i++) {
        Attr attr = (Attr) attrs.item(i);
        dst.append(' ');
        dst.append(attr.getName());
        dst.append("=\"");
        encodeHtmlAttribute(attr.getValue(), 0, attr.getValue().length(), dst);
        dst.append("\"");
      }
    }

    //
    if (!empty) {

      //
      dst.append(">");

      // Serialize children that are worth to be
      NodeList children = element.getChildNodes();
      int length = children.getLength();
      for (int i = 0; i < length; i++) {
        Node child = children.item(i);
        if (child instanceof CDATASection) {
          // writer.writeCData(((CDATASection) child).getData());
          throw new UnsupportedOperationException("Encoding CDATA not yet supported");
        } else if (child instanceof CharacterData) {
          String data = ((CharacterData)child).getData();
          encodeHtmlText(data, 0, data.length(), dst);
        } else if (child instanceof org.w3c.dom.Element) {
          encodeHtml((org.w3c.dom.Element)child, dst);
        }
      }

      //
      dst.append("</").append(tagName).append('>');
    } else {
      dst.append("/>");
    }

    //
    return dst;
  }

  public static Iterable<Node> children(final Node node) {
    return new Iterable<Node>() {
      public Iterator<Node> iterator() {
        return new Iterator<Node>() {
          int i = 0;
          NodeList children = node.getChildNodes();
          public boolean hasNext() {
            return i < children.getLength();
          }
          public Node next() {
            if (hasNext()) {
              return children.item(i++);
            } else {
              throw new NoSuchElementException();
            }
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
