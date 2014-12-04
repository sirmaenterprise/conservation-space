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

package org.activiti.engine.identity;

import org.activiti.engine.query.Query;


// TODO: Auto-generated Javadoc
/**
 * Allows programmatic querying of {@link User}.
 *
 * @author Joram Barrez
 */
public interface UserQuery extends Query<UserQuery, User> {
  
  /**
   * Only select {@link User}s with the given id/.
   *
   * @param id the id
   * @return the user query
   */
  UserQuery userId(String id);
  
  /**
   * Only select {@link User}s with the given firstName.
   *
   * @param firstName the first name
   * @return the user query
   */
  UserQuery userFirstName(String firstName);
  
  /**
   * Only select {@link User}s where the first name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   *
   * @param firstNameLike the first name like
   * @return the user query
   */
  UserQuery userFirstNameLike(String firstNameLike);
  
  /**
   * Only select {@link User}s with the given lastName.
   *
   * @param lastName the last name
   * @return the user query
   */
  UserQuery userLastName(String lastName);
  
  /**
   * Only select {@link User}s where the last name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   *
   * @param lastNameLike the last name like
   * @return the user query
   */
  UserQuery userLastNameLike(String lastNameLike);
  
  /**
   * Only those {@link User}s with the given email addres.
   *
   * @param email the email
   * @return the user query
   */
  UserQuery userEmail(String email);
  
  /**
   * Only select {@link User}s where the email matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   *
   * @param emailLike the email like
   * @return the user query
   */
  UserQuery userEmailLike(String emailLike);
  
  /**
   * Only select {@link User}s that belong to the given group.
   *
   * @param groupId the group id
   * @return the user query
   */ 
  UserQuery memberOfGroup(String groupId);

  /**
   * Only select {@link User}S that are potential starter for the given process definition.
   *
   * @param procDefId the proc def id
   * @return the user query
   */  
  public UserQuery potentialStarter(String procDefId);
  
  //sorting ////////////////////////////////////////////////////////
  
  /**
   * Order by user id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the user query
   */
  UserQuery orderByUserId();
  
  /**
   * Order by user first name (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the user query
   */
  UserQuery orderByUserFirstName();
  
  /**
   * Order by user last name (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the user query
   */
  UserQuery orderByUserLastName();
  
  /**
   * Order by user email  (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the user query
   */
  UserQuery orderByUserEmail();
}
