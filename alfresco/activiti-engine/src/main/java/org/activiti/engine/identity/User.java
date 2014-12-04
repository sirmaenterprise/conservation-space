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

import java.io.Serializable;

import org.activiti.engine.IdentityService;


// TODO: Auto-generated Javadoc
/**
 * Represents a user, used in {@link IdentityService}.
 * @author Tom Baeyens
 */
public interface User extends Serializable {

  /**
   * Gets the id.
   *
   * @return the id
   */
  String getId();
  
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  void setId(String id);

  /**
   * Gets the first name.
   *
   * @return the first name
   */
  String getFirstName();
  
  /**
   * Sets the first name.
   *
   * @param firstName the new first name
   */
  void setFirstName(String firstName);
  
  /**
   * Sets the last name.
   *
   * @param lastName the new last name
   */
  void setLastName(String lastName);
  
  /**
   * Gets the last name.
   *
   * @return the last name
   */
  String getLastName();

  /**
   * Sets the email.
   *
   * @param email the new email
   */
  void setEmail(String email);
  
  /**
   * Gets the email.
   *
   * @return the email
   */
  String getEmail();
  
  /**
   * Gets the password.
   *
   * @return the password
   */
  String getPassword(); 
  
  /**
   * Sets the password.
   *
   * @param string the new password
   */
  void setPassword(String string);
}
