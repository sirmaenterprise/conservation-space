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
 * A transformation based data output association.
 *
 * @author Esteban Robles Luna
 */
public class TransformationDataOutputAssociation extends AbstractDataAssociation {

  /** The transformation. */
  protected Expression transformation;
  
  /**
   * Instantiates a new transformation data output association.
   *
   * @param sourceRef the source ref
   * @param targetRef the target ref
   * @param transformation the transformation
   */
  public TransformationDataOutputAssociation(String sourceRef, String targetRef, Expression transformation) {
    super(sourceRef, targetRef);
    this.transformation = transformation;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.AbstractDataAssociation#evaluate(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void evaluate(ActivityExecution execution) {
    Object value = this.transformation.getValue(execution);
    execution.setVariable(this.getTarget(), value);
  }
}
