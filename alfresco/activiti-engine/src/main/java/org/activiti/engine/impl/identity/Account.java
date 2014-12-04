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

package org.activiti.engine.impl.identity;

import java.util.Map;


// TODO: Auto-generated Javadoc
/**
 * The Interface Account.
 *
 * @author Tom Baeyens
 */
public interface Account {

  /** The name alfresco. */
  String NAME_ALFRESCO = "Alfresco";
  
  /** The name google. */
  String NAME_GOOGLE = "Google";
  
  /** The name skype. */
  String NAME_SKYPE = "Skype";
  
  /** The name mail. */
  String NAME_MAIL = "Mail";

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();
  
  /**
   * Gets the username.
   *
   * @return the username
   */
  String getUsername();
  
  /**
   * Gets the password.
   *
   * @return the password
   */
  String getPassword();
  
  /**
   * Gets the details.
   *
   * @return the details
   */
  Map<String, String> getDetails();
}
