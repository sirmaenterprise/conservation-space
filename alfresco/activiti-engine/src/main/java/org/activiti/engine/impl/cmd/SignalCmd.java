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
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class SignalCmd.
 *
 * @author Tom Baeyens
 */
public class SignalCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The execution id. */
  protected String executionId;
  
  /** The signal name. */
  protected String signalName;
  
  /** The signal data. */
  protected Object signalData;
  
  /** The process variables. */
  protected final Map<String, Object> processVariables;
  
  /**
   * Instantiates a new signal cmd.
   *
   * @param executionId the execution id
   * @param signalName the signal name
   * @param signalData the signal data
   * @param processVariables the process variables
   */
  public SignalCmd(String executionId, String signalName, Object signalData, Map<String, Object> processVariables) {
    this.executionId = executionId;
    this.signalName = signalName;
    this.signalData = signalData;
    this.processVariables = processVariables;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) { 
    if(executionId == null) {
      throw new ActivitiException("executionId is null");
    }
    
    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);
    
    if (execution==null) {
      throw new ActivitiException("execution "+executionId+" doesn't exist");
    }
    
    if(processVariables != null) {
      execution.setVariables(processVariables);
    }
    
    execution.signal(signalName, signalData);
    return null;
  }

}
