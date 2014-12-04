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

import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


// TODO: Auto-generated Javadoc
/**
 * The Class GetUserAccountCmd.
 *
 * @author Tom Baeyens
 */
public class GetUserAccountCmd implements Command<Account>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The user id. */
  protected String userId;
  
  /** The user password. */
  protected String userPassword;
  
  /** The account name. */
  protected String accountName;
  
  /**
   * Instantiates a new gets the user account cmd.
   *
   * @param userId the user id
   * @param userPassword the user password
   * @param accountName the account name
   */
  public GetUserAccountCmd(String userId, String userPassword, String accountName) {
    this.userId = userId;
    this.userPassword = userPassword;
    this.accountName = accountName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Account execute(CommandContext commandContext) {
    return commandContext
      .getIdentityInfoManager()
      .findUserAccountByUserIdAndKey(userId, userPassword, accountName);
  }
}
