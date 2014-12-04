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
package org.activiti.engine;

import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.identity.Account;


// TODO: Auto-generated Javadoc
/**
 * Service to manage {@link User}s and {@link Group}s.
 * 
 * @author Tom Baeyens
 */
public interface IdentityService {

  /**
   * Creates a new user. The user is transient and must be saved using
   *
   * @param userId id for the new user, cannot be null.
   * @return the user
   * {@link #saveUser(User)}.
   */
  User newUser(String userId);
  
  /**
   * Saves the user. If the user already existed, the user is updated.
   *
   * @param user user to save, cannot be null.
   */
  void saveUser(User user);
  
  /**
   * Creates a {@link UserQuery} that allows to programmatically query the users.
   *
   * @return the user query
   */
  UserQuery createUserQuery();
  
  /**
   * Delete user.
   *
   * @param userId id of user to delete, cannot be null. When an id is passed
   * for an unexisting user, this operation is ignored.
   */
  void deleteUser(String userId);
  
  /**
   * Creates a new group. The group is transient and must be saved using
   *
   * @param groupId id for the new group, cannot be null.
   * @return the group
   * {@link #saveGroup(Group)}.
   */
  Group newGroup(String groupId);
  
  /**
   * Creates a {@link GroupQuery} thats allows to programmatically query the groups.
   *
   * @return the group query
   */
  GroupQuery createGroupQuery();
  
  /**
   * Saves the group. If the group already existed, the group is updated.
   *
   * @param group group to save. Cannot be null.
   */
  void saveGroup(Group group);
  
  /**
   * Deletes the group. When no group exists with the given id, this operation
   * is ignored.
   * @param groupId id of the group that should be deleted, cannot be null.
   */
  void deleteGroup(String groupId);

  /**
   * Creates the membership.
   *
   * @param userId the userId, cannot be null.
   * @param groupId the groupId, cannot be null.
   */
  void createMembership(String userId, String groupId);
  
  /**
   * Delete the membership of the user in the group. When the group or user don't exist 
   * or when the user is not a member of the group, this operation is ignored.
   * @param userId the user's id, cannot be null.
   * @param groupId the group's id, cannot be null.
   */
  void deleteMembership(String userId, String groupId);

  /**
   * Checks if the password is valid for the given user. Arguments userId
   * and password are nullsafe.
   *
   * @param userId the user id
   * @param password the password
   * @return true, if successful
   */
  boolean checkPassword(String userId, String password);

  /**
   * Passes the authenticated user id for this particular thread.
   * All service method (from any service) invocations done by the same
   * thread will have access to this authenticatedUserId.
   *
   * @param authenticatedUserId the new authenticated user id
   */
  void setAuthenticatedUserId(String authenticatedUserId);
  
  /**
   * Sets the picture for a given user.
   *
   * @param userId the user id
   * @param picture can be null to delete the picture.
   */
  void setUserPicture(String userId, Picture picture);

  /**
   * Retrieves the picture for a given user.
   *
   * @param userId the user id
   * @return the user picture
   * @returns null if the user doesn't have a picture.
   */
  Picture getUserPicture(String userId);

  /**
   * Generic extensibility key-value pairs associated with a user.
   *
   * @param userId the user id
   * @param key the key
   * @param value the value
   */
  void setUserInfo(String userId, String key, String value);
  
  /**
   * Generic extensibility key-value pairs associated with a user.
   *
   * @param userId the user id
   * @param key the key
   * @return the user info
   */
  String getUserInfo(String userId, String key);

  /**
   * Generic extensibility keys associated with a user.
   *
   * @param userId the user id
   * @return the user info keys
   */
  List<String> getUserInfoKeys(String userId);

  /**
   * Delete an entry of the generic extensibility key-value pairs associated with a user.
   *
   * @param userId the user id
   * @param key the key
   */
  void deleteUserInfo(String userId, String key);

  /**
   * Store account information for a remote system.
   *
   * @param userId the user id
   * @param userPassword the user password
   * @param accountName the account name
   * @param accountUsername the account username
   * @param accountPassword the account password
   * @param accountDetails the account details
   */
  void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails);
  
  /**
   * Get account names associated with the given user.
   *
   * @param userId the user id
   * @return the user account names
   */
  List<String> getUserAccountNames(String userId);

  /**
   * Get account information associated with a user.
   *
   * @param userId the user id
   * @param userPassword the user password
   * @param accountName the account name
   * @return the user account
   */
  Account getUserAccount(String userId, String userPassword, String accountName);

  /**
   * Delete an entry of the generic extensibility key-value pairs associated with a user.
   *
   * @param userId the user id
   * @param accountName the account name
   */
  void deleteUserAccount(String userId, String accountName);
}
