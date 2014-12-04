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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class GetExecutionVariablesCmd.
 *
 * @author Tom Baeyens
 */
public class GetExecutionVariablesCmd implements Command<Map<String, Object>>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The execution id. */
  protected String executionId;
  
  /** The variable names. */
  protected Collection<String> variableNames;
  
  /** The is local. */
  protected boolean isLocal;

  /**
   * Instantiates a new gets the execution variables cmd.
   *
   * @param executionId the execution id
   * @param variableNames the variable names
   * @param isLocal the is local
   */
  public GetExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    this.executionId = executionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Map<String, Object> execute(CommandContext commandContext) {
    if(executionId == null) {
      throw new ActivitiException("executionId is null");
    }
    
    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);
    
    if (execution==null) {
      throw new ActivitiException("execution "+executionId+" doesn't exist");
    }

    Map<String, Object> executionVariables;
    if (isLocal) {
      executionVariables = execution.getVariablesLocal();
    } else {
      executionVariables = execution.getVariables();
    }
    
    return executionVariables;
  }
}
