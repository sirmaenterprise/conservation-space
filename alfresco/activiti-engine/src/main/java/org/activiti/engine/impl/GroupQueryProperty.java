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

import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.query.QueryProperty;



// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used by the {@link GroupQuery}.
 * 
 * @author Joram Barrez
 */
public class GroupQueryProperty implements QueryProperty {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, GroupQueryProperty> properties = new HashMap<String, GroupQueryProperty>();

  /** The Constant GROUP_ID. */
  public static final GroupQueryProperty GROUP_ID = new GroupQueryProperty("G.ID_");
  
  /** The Constant NAME. */
  public static final GroupQueryProperty NAME = new GroupQueryProperty("G.NAME_");
  
  /** The Constant TYPE. */
  public static final GroupQueryProperty TYPE = new GroupQueryProperty("G.TYPE_");
  
  /** The name. */
  private String name;

  /**
   * Instantiates a new group query property.
   *
   * @param name the name
   */
  public GroupQueryProperty(String name) {
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
   * @return the group query property
   */
  public static GroupQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
