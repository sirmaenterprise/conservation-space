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
 * The Class DeleteMembershipCmd.
 *
 * @author Tom Baeyens
 */
public class DeleteMembershipCmd implements Command<Void>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The user id. */
  String userId;
  
  /** The group id. */
  String groupId;

  /**
   * Instantiates a new delete membership cmd.
   *
   * @param userId the user id
   * @param groupId the group id
   */
  public DeleteMembershipCmd(String userId, String groupId) {
    this.userId = userId;
    this.groupId = groupId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Void execute(CommandContext commandContext) {
    if(userId == null) {
      throw new ActivitiException("userId is null");
    }
    if(groupId == null) {
      throw new ActivitiException("groupId is null");
    }
    
    commandContext
      .getMembershipManager()
      .deleteMembership(userId, groupId);
    
    return null;    
  }

}
