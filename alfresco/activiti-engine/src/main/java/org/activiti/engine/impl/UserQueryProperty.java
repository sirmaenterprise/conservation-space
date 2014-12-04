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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.query.QueryProperty;



// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used by the {@link UserQuery}.
 * 
 * @author Joram Barrez
 */
public class UserQueryProperty implements QueryProperty {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, UserQueryProperty> properties = new HashMap<String, UserQueryProperty>();

  /** The Constant USER_ID. */
  public static final UserQueryProperty USER_ID = new UserQueryProperty("U.ID_");
  
  /** The Constant FIRST_NAME. */
  public static final UserQueryProperty FIRST_NAME = new UserQueryProperty("U.FIRST_");
  
  /** The Constant LAST_NAME. */
  public static final UserQueryProperty LAST_NAME = new UserQueryProperty("U.LAST_");
  
  /** The Constant EMAIL. */
  public static final UserQueryProperty EMAIL = new UserQueryProperty("U.EMAIL_");
  
  /** The name. */
  private String name;

  /**
   * Instantiates a new user query property.
   *
   * @param name the name
   */
  public UserQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.query.QueryProperty#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Find by name.
   *
   * @param propertyName the property name
   * @return the user query property
   */
  public static UserQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
