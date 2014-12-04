/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.bpmn.data;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

// TODO: Auto-generated Javadoc
/**
 * A data association (Input or Output) between a source and a target.
 *
 * @author Esteban Robles Luna
 */
public abstract class AbstractDataAssociation {

  /** The source. */
  protected String source;

  /** The source expression. */
  protected Expression sourceExpression;

  /** The target. */
  protected String target;
  
  /**
   * Instantiates a new abstract data association.
   *
   * @param source the source
   * @param target the target
   */
  protected AbstractDataAssociation(String source, String target) {
    this.source = source;
    this.target = target;
  }

  /**
   * Instantiates a new abstract data association.
   *
   * @param sourceExpression the source expression
   * @param target the target
   */
  protected AbstractDataAssociation(Expression sourceExpression, String target) {
    this.sourceExpression = sourceExpression;
    this.target = target;
  }

  /**
   * Evaluate.
   *
   * @param execution the execution
   */
  public abstract void evaluate(ActivityExecution execution);
  
  /**
   * Gets the source.
   *
   * @return the source
   */
  public String getSource() {
    return source;
  }
  
  /**
   * Gets the target.
   *
   * @return the target
   */
  public String getTarget() {
    return target;
  }

  
  /**
   * Gets the source expression.
   *
   * @return the source expression
   */
  public Expression getSourceExpression() {
    return sourceExpression;
  }
}
