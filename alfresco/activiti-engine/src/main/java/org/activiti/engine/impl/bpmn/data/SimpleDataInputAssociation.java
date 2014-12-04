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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

// TODO: Auto-generated Javadoc
/**
 * A simple data input association between a source and a target with assignments.
 *
 * @author Esteban Robles Luna
 */
public class SimpleDataInputAssociation extends AbstractDataAssociation {

  /** The assignments. */
  protected List<Assignment> assignments = new ArrayList<Assignment>();
  
  /**
   * Instantiates a new simple data input association.
   *
   * @param sourceExpression the source expression
   * @param target the target
   */
  public SimpleDataInputAssociation(Expression sourceExpression, String target) {
    super(sourceExpression, target);
  }

  /**
   * Instantiates a new simple data input association.
   *
   * @param source the source
   * @param target the target
   */
  public SimpleDataInputAssociation(String source, String target) {
    super(source, target);
  }
  
  /**
   * Adds the assignment.
   *
   * @param assignment the assignment
   */
  public void addAssignment(Assignment assignment) {
    this.assignments.add(assignment);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.AbstractDataAssociation#evaluate(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void evaluate(ActivityExecution execution) {
    for (Assignment assignment : this.assignments) {
      assignment.evaluate(execution);
    }
  }
}
