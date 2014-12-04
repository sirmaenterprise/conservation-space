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

package org.activiti.engine.impl.pvm.runtime;

import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Class AtomicOperationDeleteCascade.
 *
 * @author Tom Baeyens
 */
public class AtomicOperationDeleteCascade implements AtomicOperation {
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.AtomicOperation#isAsync(org.activiti.engine.impl.pvm.runtime.InterpretableExecution)
   */
  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.AtomicOperation#execute(org.activiti.engine.impl.pvm.runtime.InterpretableExecution)
   */
  public void execute(InterpretableExecution execution) {
    InterpretableExecution firstLeaf = findFirstLeaf(execution);
    
    if (firstLeaf.getSubProcessInstance()!=null) {
      firstLeaf.getSubProcessInstance().deleteCascade(execution.getDeleteReason());
    }

    firstLeaf.performOperation(AtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END);
  }

  /**
   * Find first leaf.
   *
   * @param execution the execution
   * @return the interpretable execution
   */
  @SuppressWarnings("unchecked")
  protected InterpretableExecution findFirstLeaf(InterpretableExecution execution) {
    List<InterpretableExecution> executions = (List<InterpretableExecution>) execution.getExecutions();
    if (executions.size()>0) {
      return findFirstLeaf(executions.get(0));
    }
    return execution;
  }
}
