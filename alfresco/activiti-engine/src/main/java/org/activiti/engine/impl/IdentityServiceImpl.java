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
package org.activiti.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.cmd.CheckPassword;
import org.activiti.engine.impl.cmd.CreateGroupCmd;
import org.activiti.engine.impl.cmd.CreateGroupQueryCmd;
import org.activiti.engine.impl.cmd.CreateMembershipCmd;
import org.activiti.engine.impl.cmd.CreateUserCmd;
import org.activiti.engine.impl.cmd.CreateUserQueryCmd;
import org.activiti.engine.impl.cmd.DeleteGroupCmd;
import org.activiti.engine.impl.cmd.DeleteMembershipCmd;
import org.activiti.engine.impl.cmd.DeleteUserCmd;
import org.activiti.engine.impl.cmd.DeleteUserInfoCmd;
import org.activiti.engine.impl.cmd.GetUserAccountCmd;
import org.activiti.engine.impl.cmd.GetUserInfoCmd;
import org.activiti.engine.impl.cmd.GetUserInfoKeysCmd;
import org.activiti.engine.impl.cmd.GetUserPictureCmd;
import org.activiti.engine.impl.cmd.SaveGroupCmd;
import org.activiti.engine.impl.cmd.SaveUserCmd;
import org.activiti.engine.impl.cmd.SetUserInfoCmd;
import org.activiti.engine.impl.cmd.SetUserPictureCmd;
import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class IdentityServiceImpl.
 *
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends ServiceImpl implements IdentityService {
  
  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#newGroup(java.lang.String)
   */
  public Group newGroup(String groupId) {
    return commandExecutor.execute(new CreateGroupCmd(groupId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#newUser(java.lang.String)
   */
  public User newUser(String userId) {
    return commandExecutor.execute(new CreateUserCmd(userId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#saveGroup(org.activiti.engine.identity.Group)
   */
  public void saveGroup(Group group) {
    commandExecutor.execute(new SaveGroupCmd((GroupEntity) group));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#saveUser(org.activiti.engine.identity.User)
   */
  public void saveUser(User user) {
    commandExecutor.execute(new SaveUserCmd(user));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#createUserQuery()
   */
  public UserQuery createUserQuery() {
    return commandExecutor.execute(new CreateUserQueryCmd());
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#createGroupQuery()
   */
  public GroupQuery createGroupQuery() {
    return commandExecutor.execute(new CreateGroupQueryCmd());
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#createMembership(java.lang.String, java.lang.String)
   */
  public void createMembership(String userId, String groupId) {
    commandExecutor.execute(new CreateMembershipCmd(userId, groupId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#deleteGroup(java.lang.String)
   */
  public void deleteGroup(String groupId) {
    commandExecutor.execute(new DeleteGroupCmd(groupId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#deleteMembership(java.lang.String, java.lang.String)
   */
  public void deleteMembership(String userId, String groupId) {
    commandExecutor.execute(new DeleteMembershipCmd(userId, groupId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#checkPassword(java.lang.String, java.lang.String)
   */
  public boolean checkPassword(String userId, String password) {
    return commandExecutor.execute(new CheckPassword(userId, password));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#deleteUser(java.lang.String)
   */
  public void deleteUser(String userId) {
    commandExecutor.execute(new DeleteUserCmd(userId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#setUserPicture(java.lang.String, org.activiti.engine.identity.Picture)
   */
  public void setUserPicture(String userId, Picture picture) {
    commandExecutor.execute(new SetUserPictureCmd(userId, picture));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#getUserPicture(java.lang.String)
   */
  public Picture getUserPicture(String userId) {
    return commandExecutor.execute(new GetUserPictureCmd(userId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#setAuthenticatedUserId(java.lang.String)
   */
  public void setAuthenticatedUserId(String authenticatedUserId) {
    Authentication.setAuthenticatedUserId(authenticatedUserId);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#getUserInfo(java.lang.String, java.lang.String)
   */
  public String getUserInfo(String userId, String key) {
    return commandExecutor.execute(new GetUserInfoCmd(userId, key));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#getUserInfoKeys(java.lang.String)
   */
  public List<String> getUserInfoKeys(String userId) {
    return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERINFO));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#getUserAccountNames(java.lang.String)
   */
  public List<String> getUserAccountNames(String userId) {
    return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERACCOUNT));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#setUserInfo(java.lang.String, java.lang.String, java.lang.String)
   */
  public void setUserInfo(String userId, String key, String value) {
    commandExecutor.execute(new SetUserInfoCmd(userId, key, value));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#deleteUserInfo(java.lang.String, java.lang.String)
   */
  public void deleteUserInfo(String userId, String key) {
    commandExecutor.execute(new DeleteUserInfoCmd(userId, key));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#deleteUserAccount(java.lang.String, java.lang.String)
   */
  public void deleteUserAccount(String userId, String accountName) {
    commandExecutor.execute(new DeleteUserInfoCmd(userId, accountName));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#getUserAccount(java.lang.String, java.lang.String, java.lang.String)
   */
  public Account getUserAccount(String userId, String userPassword, String accountName) {
    return commandExecutor.execute(new GetUserAccountCmd(userId, userPassword, accountName));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.IdentityService#setUserAccount(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
   */
  public void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
    commandExecutor.execute(new SetUserInfoCmd(userId, userPassword, accountName, accountUsername, accountPassword, accountDetails));
  }
}
