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

package juzu.impl.router;

import juzu.impl.router.regex.GroupType;
import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REVisitor;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CaptureGroupTransformation extends REVisitor<RuntimeException> {

  /** . */
  private int depth;

  /** Top level group per disjunction. */
  private LinkedList<RENode.Group> groups;

  CaptureGroupTransformation() {
    this.depth = 0;
    this.groups = new LinkedList<RENode.Group>();
  }

  @Override
  protected void visit(RENode.Disjunction disjunction) throws RuntimeException {
    if (disjunction.hasAlternative()) {
      RENode.Alternative alternative = disjunction.getAlternative();
      if (alternative != null) {
        alternative.accept(this);

        //
        if (depth == 0) {
          if (groups.size() == 1 && groups.get(0).getQuantifier() == null) {
            // Do nothing
          }
          else {
            // We make all the top level groups non capturing
            for (RENode.Group group : groups) {
              group.setType(GroupType.NON_CAPTURING_GROUP);
            }

            // We add a capturing group for the disjunction
            RENode.Disjunction disjunction1 = new RENode.Disjunction((RENode.Alternative)null);
            RENode.Group group = new RENode.Group(disjunction1, GroupType.CAPTURING_GROUP);
            RENode.Alternative alternative1 = new RENode.Alternative(group);
            alternative.replaceBy(alternative1);
            disjunction1.setAlternative(alternative);
          }

          //
          groups.clear();
        }
      }
      else {
        if (depth == 0) {
          disjunction.setAlternative(
              new RENode.Alternative(
                  new RENode.Group(
                      new RENode.Disjunction(), GroupType.CAPTURING_GROUP)));
        }
      }
    }

    //
    if (disjunction.hasNext()) {
      RENode.Disjunction next = disjunction.getNext();
      if (next != null) {
        next.accept(this);
      }
      else {
        if (depth == 0) {
          disjunction.setNext(
              new RENode.Disjunction(
                  new RENode.Alternative(
                      new RENode.Group(
                          new RENode.Disjunction(), GroupType.CAPTURING_GROUP))));
        }
      }
    }
  }

  @Override
  protected void visit(RENode.Group expr) throws RuntimeException {
    if (depth == 0) {
      // We collect all capturing top level capturing groups
      if (expr.getType() == GroupType.CAPTURING_GROUP) {
        groups.add(expr);
      }
    }
    else {
      // We make nested capturing group as non capturing
      if (expr.getType() == GroupType.CAPTURING_GROUP) {
        expr.setType(GroupType.NON_CAPTURING_GROUP);
      }
    }

    //
    depth++;
    super.visit(expr);
    depth--;
  }
}
