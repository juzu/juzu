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

package juzu.impl.common;

/**
 * <p>A qualified name that is a qualifier and a name. It can be seen as a simplified version of an XML QName that
 * retains only the prefix (qualifier) and the local name (name) and leaves out the namespace.</p>
 * <p>Qualified names have a string representation that is equals to the concatenation of the qualifier and name
 * separated by a colon (:) character. When the </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class QualifiedName implements Comparable<QualifiedName> {

  /**
   * Parse the string representation of a qname and returns a qualified name.
   *
   * @param qname the qname to parse
   * @return the corresponding qualified name
   * @throws NullPointerException if the qname argument is null
   * @throws IllegalArgumentException if the qname argument contains more than one colon character
   */
  public static QualifiedName parse(String qname) throws NullPointerException, IllegalArgumentException {
    if (qname == null) {
      throw new NullPointerException("No null argument accepted");
    }
    if (qname.length() > 0) {
      int index = qname.indexOf(':');
      if (index > -1) {
        return create(qname.substring(0, index), qname.substring(index + 1));
      }
    }
    return create(qname);
  }

  /**
   * Creates a qualified name.
   *
   * @param qualifier the qualifier
   * @param name the name
   * @return the qualified name
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if any argument contains a colon character
   */
  public static QualifiedName create(String qualifier, String name) throws NullPointerException, IllegalArgumentException {
    return new QualifiedName(qualifier, name);
  }

  /**
   * Creates a qualified name with an empty string qualifier.
   *
   * @param name the name
   * @return the qualified name
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if any argument contains a colon character
   */
  public static QualifiedName create(String name) throws NullPointerException, IllegalArgumentException {
    return new QualifiedName(name);
  }

  /** The qualifier. */
  private final String qualifier;

  /** The name. */
  private final String name;

  /** The cached hash code. */
  private int hashCode;

  private QualifiedName(String name) throws NullPointerException, IllegalArgumentException {
    this("", name);
  }

  private QualifiedName(String qualifier, String name) throws NullPointerException, IllegalArgumentException {
    if (qualifier == null) {
      throw new NullPointerException("No null prefix accepted");
    }
    if (qualifier.indexOf(':') != -1) {
      throw new IllegalArgumentException("The name '" + qualifier + "' must not contain a colon character");
    }
    if (name == null) {
      throw new NullPointerException("No null prefix accepted");
    }
    if (name.indexOf(':') != -1) {
      throw new IllegalArgumentException("The name '" + name + "' must not contain a colon character");
    }

    //
    this.qualifier = qualifier;
    this.name = name;
    this.hashCode = qualifier.hashCode() ^ name.hashCode();
  }

  /**
   * Returns the qualifier.
   *
   * @return the qualifier
   */
  public String getQualifier() {
    return qualifier;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the string representation.
   *
   * @return the string representation
   */
  public String getValue() {
    if (qualifier.isEmpty()) {
      return name;
    }
    else {
      return qualifier + ":" + name;
    }
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof QualifiedName) {
      QualifiedName that = (QualifiedName)obj;
      return qualifier.equals(that.qualifier) && name.equals(that.name);
    }
    return false;
  }

  public int compareTo(QualifiedName o) {
    if (o == null) {
      throw new NullPointerException("No null argument accepted");
    }
    else if (o == this) {
      return 0;
    }
    else {
      int qualifierComparison = qualifier.compareTo(o.qualifier);
      if (qualifierComparison == 0) {
        return name.compareTo(o.name);
      }
      else {
        return qualifierComparison;
      }
    }
  }

  @Override
  public String toString() {
    return "QualifiedName[prefix=" + qualifier + ",name=" + name + "]";
  }
}
