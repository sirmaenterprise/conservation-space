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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class DeleteIdentityLinkForProcessDefinitionCmd.
 *
 * @author Tijs Rademakers
 */
public class DeleteIdentityLinkForProcessDefinitionCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The user id. */
  protected String userId;
  
  /** The group id. */
  protected String groupId;
  
  /**
   * Instantiates a new delete identity link for process definition cmd.
   *
   * @param processDefinitionId the process definition id
   * @param userId the user id
   * @param groupId the group id
   */
  public DeleteIdentityLinkForProcessDefinitionCmd(String processDefinitionId, String userId, String groupId) {
    validateParams(userId, groupId, processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    this.userId = userId;
    this.groupId = groupId;
  }
  
  /**
   * Validate params.
   *
   * @param userId the user id
   * @param groupId the group id
   * @param processDefinitionId the process definition id
   */
  protected void validateParams(String userId, String groupId, String processDefinitionId) {
    if(processDefinitionId == null) {
      throw new ActivitiException("processDefinitionId is null");
    }
    
    if (userId == null && groupId == null) {
      throw new ActivitiException("userId and groupId cannot both be null");
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Void execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
        .getCommandContext()
        .getProcessDefinitionManager()
        .findLatestProcessDefinitionById(processDefinitionId);
      
    if (processDefinition == null) {
      throw new ActivitiException("Cannot find process definition with id " + processDefinitionId);
    }
    
    processDefinition.deleteIdentityLink(userId, groupId);
    
    return null;  
  }
  
}
