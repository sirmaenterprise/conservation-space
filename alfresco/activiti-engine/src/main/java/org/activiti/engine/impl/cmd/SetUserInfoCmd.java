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

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class SetUserInfoCmd.
 *
 * @author Tom Baeyens
 */
public class SetUserInfoCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The user id. */
  protected String userId;
  
  /** The user password. */
  protected String userPassword;
  
  /** The type. */
  protected String type;
  
  /** The key. */
  protected String key;
  
  /** The value. */
  protected String value;
  
  /** The account password. */
  protected String accountPassword;
  
  /** The account details. */
  protected Map<String, String> accountDetails;
  
  /**
   * Instantiates a new sets the user info cmd.
   *
   * @param userId the user id
   * @param key the key
   * @param value the value
   */
  public SetUserInfoCmd(String userId, String key, String value) {
    this.userId = userId;
    this.type = IdentityInfoEntity.TYPE_USERINFO;
    this.key = key;
    this.value = value;
  }

  /**
   * Instantiates a new sets the user info cmd.
   *
   * @param userId the user id
   * @param userPassword the user password
   * @param accountName the account name
   * @param accountUsername the account username
   * @param accountPassword the account password
   * @param accountDetails the account details
   */
  public SetUserInfoCmd(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
    this.userId = userId;
    this.userPassword = userPassword;
    this.type = IdentityInfoEntity.TYPE_USERACCOUNT;
    this.key = accountName;
    this.value = accountUsername;
    this.accountPassword = accountPassword;
    this.accountDetails = accountDetails;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {
    commandContext
      .getIdentityInfoManager()
      .setUserInfo(userId, userPassword, type, key, value, accountPassword, accountDetails);
    return null;
  }
}
