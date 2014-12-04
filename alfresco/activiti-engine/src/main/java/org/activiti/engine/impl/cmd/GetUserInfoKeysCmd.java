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
import java.util.List;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The Class GetUserInfoKeysCmd.
 *
 * @author Tom Baeyens
 */
public class GetUserInfoKeysCmd implements Command<List<String>>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The user id. */
  protected String userId;
  
  /** The user info type. */
  protected String userInfoType;
  
  /**
   * Instantiates a new gets the user info keys cmd.
   *
   * @param userId the user id
   * @param userInfoType the user info type
   */
  public GetUserInfoKeysCmd(String userId, String userInfoType) {
    this.userId = userId;
    this.userInfoType = userInfoType;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public List<String> execute(CommandContext commandContext) {
    return commandContext
      .getIdentityInfoManager()
      .findUserInfoKeysByUserIdAndType(userId, userInfoType);
  }
}
