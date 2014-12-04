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
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The Class CreateGroupCmd.
 *
 * @author Tom Baeyens
 */
public class CreateGroupCmd implements Command<Group>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The group id. */
  protected String groupId;
  
  /**
   * Instantiates a new creates the group cmd.
   *
   * @param groupId the group id
   */
  public CreateGroupCmd(String groupId) {
    if(groupId == null) {
      throw new ActivitiException("groupId is null");
    }
    this.groupId = groupId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Group execute(CommandContext commandContext) {
    return commandContext
      .getGroupManager()
      .createNewGroup(groupId);
  }

}
