package juzu.impl.router;

import juzu.impl.router.regex.RE;

/**
 * A mere reference to an expression with an index.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class RERef {

  /** . */
  final int index;

  /** . */
  final RE re;

  RERef(int index, RE re) {
    this.index = index;
    this.re = re;
  }
}
