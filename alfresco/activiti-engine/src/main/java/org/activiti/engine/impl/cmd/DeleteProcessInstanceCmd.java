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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The Class DeleteProcessInstanceCmd.
 *
 * @author Joram Barrez
 */
public class DeleteProcessInstanceCmd implements Command<Void>, Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The delete reason. */
  protected String deleteReason;

  /**
   * Instantiates a new delete process instance cmd.
   *
   * @param processInstanceId the process instance id
   * @param deleteReason the delete reason
   */
  public DeleteProcessInstanceCmd(String processInstanceId, String deleteReason) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Void execute(CommandContext commandContext) { 
    if(processInstanceId == null) {
      throw new ActivitiException("processInstanceId is null");
    }
    
    commandContext
      .getExecutionManager()
      .deleteProcessInstance(processInstanceId, deleteReason);
    return null;
  }

}
