package juzu.impl.router;

/**
 * Handles {@link RouteParser} callbacks.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface RouteParserHandler {

  void openSegment();

  void segmentChunk(CharSequence s, int from, int to);

  void closeSegment();

  void closePath();

  void query();

  void queryParamLHS(CharSequence s, int from, int to);

  void endQueryParam();

  void queryParamRHS();

  void queryParamRHS(CharSequence s, int from, int to);

  void openExpr();

  void pattern(CharSequence s, int from, int to);

  void modifiers(CharSequence s, int from, int to);

  void ident(CharSequence s, int from, int to);

  void closeExpr();

}
